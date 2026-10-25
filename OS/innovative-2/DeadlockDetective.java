

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.DoubleConsumer;

public class DeadlockDetective {

    // Swing application for detecting deadlocks and exploring recovery options.

    // Convert a resource index to its display label (A, B, C, ...).
    static String rn(int r) {
        return String.valueOf((char) ('A' + r));
    }

    static String vec(int[] v) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < v.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(v[i]);
        }
        return sb.append("]").toString();
    }

    static String names(List<Integer> l) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < l.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("P").append(l.get(i));
        }
        return sb.toString();
    }

    static String andJoin(List<Integer> l) {
        if (l.size() == 1) return "P" + l.get(0);
        return names(l.subList(0, l.size() - 1)) + " and P" + l.get(l.size() - 1);
    }

    static String chain(List<Integer> l) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < l.size(); i++) {
            if (i > 0) sb.append(" -> ");
            sb.append("P").append(l.get(i));
        }
        return sb.toString();
    }

    static boolean isZero(int[] v) {
        for (int x : v) if (x != 0) return false;
        return true;
    }

    static boolean leq(int[] a, int[] b) {
        for (int i = 0; i < a.length; i++) if (a[i] > b[i]) return false;
        return true;
    }

    static int sum(int[] v) {
        int s = 0;
        for (int x : v) s += x;
        return s;
    }

    static int[][] deep(int[][] a) {
        int[][] c = new int[a.length][];
        for (int i = 0; i < a.length; i++) c[i] = a[i].clone();
        return c;
    }

    static final class Step {
        int proc;
        int[] req, before, after;
    }

    // Stores the detection results and the evidence used by the UI and report.
    static final class Analysis {
        int n, m;
        int[][] alloc, req;
        int[] avail, total, finalWork;
        boolean[] finish;
        List<Integer> startFinished = new ArrayList<Integer>();   
        List<Step> steps = new ArrayList<Step>();
        List<Integer> order = new ArrayList<Integer>();           
        List<Integer> dead = new ArrayList<Integer>();            
        List<int[]> waits = new ArrayList<int[]>();               
        List<List<Integer>> cycles = new ArrayList<List<Integer>>();
    }

    static final class Option {
        List<Integer> victims = new ArrayList<Integer>();
        int cost;                                  
        List<Integer> order = new ArrayList<Integer>();
    }

    // Implements the deadlock detection, wait-cycle search, and recovery logic.
    static final class Detector {

        // Simulate process completion by releasing allocations whenever requests fit.
        static Analysis analyze(int[][] alloc, int[][] req, int[] avail) {
            Analysis a = new Analysis();
            a.n = alloc.length;
            a.m = avail.length;
            a.alloc = deep(alloc);
            a.req = deep(req);
            a.avail = avail.clone();
            a.total = avail.clone();
            for (int[] row : alloc) for (int r = 0; r < a.m; r++) a.total[r] += row[r];

            int[] work = avail.clone();                       
            a.finish = new boolean[a.n];
            for (int i = 0; i < a.n; i++) {
                if (isZero(alloc[i])) {                       
                    a.finish[i] = true;
                    a.startFinished.add(i);
                    a.order.add(i);
                }
            }
            boolean progress = true;
            while (progress) {                                
                progress = false;
                for (int i = 0; i < a.n; i++) {
                    if (!a.finish[i] && leq(req[i], work)) {
                        Step s = new Step();
                        s.proc = i;
                        s.req = req[i].clone();
                        s.before = work.clone();
                        for (int r = 0; r < a.m; r++) work[r] += alloc[i][r];
                        s.after = work.clone();
                        a.steps.add(s);
                        a.finish[i] = true;
                        a.order.add(i);
                        progress = true;
                        break;
                    }
                }
            }
            a.finalWork = work;
            for (int i = 0; i < a.n; i++) if (!a.finish[i]) a.dead.add(i);   

            for (int i : a.dead)
                for (int r = 0; r < a.m; r++)
                    if (req[i][r] > work[r])
                        for (int j : a.dead)
                            if (j != i && alloc[j][r] > 0) a.waits.add(new int[]{i, j, r});
            int[] color = new int[a.n];
            Set<String> seen = new HashSet<String>();
            for (int s : a.dead) if (color[s] == 0) dfs(a, s, color, new ArrayList<Integer>(), seen);
            return a;
        }

        // Find cycles in the wait-for relationships among unfinished processes.
        static void dfs(Analysis a, int u, int[] color, List<Integer> path, Set<String> seen) {
            color[u] = 1;
            path.add(u);
            for (int[] w : a.waits) {
                if (w[0] != u) continue;
                int v = w[1];
                if (color[v] == 0) dfs(a, v, color, path, seen);
                else if (color[v] == 1) {
                    List<Integer> cyc = new ArrayList<Integer>(path.subList(path.indexOf(v), path.size()));
                    List<Integer> key = new ArrayList<Integer>(cyc);
                    Collections.sort(key);
                    if (seen.add(key.toString())) a.cycles.add(cyc);
                }
            }
            path.remove(path.size() - 1);
            color[u] = 2;
        }

        // Try victim sets from smallest to largest and keep valid low-cost recoveries.
        static List<Option> recover(Analysis a) {
            List<Option> res = new ArrayList<Option>();
            int d = a.dead.size();
            for (int k = 1; k <= d && res.isEmpty(); k++) {
                for (int mask = 1; mask < (1 << d); mask++) {
                    if (Integer.bitCount(mask) != k) continue;
                    int[][] al = deep(a.alloc), rq = deep(a.req);
                    int[] av = a.avail.clone();
                    Option o = new Option();
                    for (int b = 0; b < d; b++) {
                        if (((mask >> b) & 1) == 0) continue;
                        int p = a.dead.get(b);
                        o.victims.add(p);
                        for (int r = 0; r < a.m; r++) {
                            av[r] += al[p][r];                  
                            o.cost += al[p][r];
                            al[p][r] = 0;
                            rq[p][r] = 0;
                        }
                    }
                    Analysis t = analyze(al, rq, av);
                    if (t.dead.isEmpty()) {
                        for (int p : t.order) if (!o.victims.contains(p)) o.order.add(p);
                        res.add(o);
                    }
                }
            }
            Collections.sort(res, new Comparator<Option>() {
                public int compare(Option x, Option y) {
                    return Integer.compare(x.cost, y.cost);
                }
            });
            return res.size() > 3 ? new ArrayList<Option>(res.subList(0, 3)) : res;
        }

        static List<Object[]> stepRows(Analysis a) {
            List<Object[]> rows = new ArrayList<Object[]>();
            rows.add(new Object[]{"Init", "-", "-", "Work = Available",
                    a.startFinished.isEmpty() ? "No process finished yet"
                            : "Hold nothing, finished at start: " + names(a.startFinished), vec(a.avail)});
            int k = 1;
            for (Step s : a.steps) {
                rows.add(new Object[]{String.valueOf(k++), new Tag("P" + s.proc, s.proc), vec(s.req), vec(s.before),
                        "Request fits in Work: finishes and releases " + vec(a.alloc[s.proc]), vec(s.after)});
            }
            for (int i : a.dead) {
                StringBuilder sh = new StringBuilder();
                for (int r = 0; r < a.m; r++)
                    if (a.req[i][r] > a.finalWork[r]) sh.append(sh.length() > 0 ? ", " : "").append(rn(r));
                rows.add(new Object[]{"Stop", new Tag("P" + i, i), vec(a.req[i]), vec(a.finalWork),
                        "Request does not fit (short of " + sh + "): blocked", vec(a.finalWork)});
            }
            return rows;
        }

        static List<String> explain(Analysis a) {
            List<String> out = new ArrayList<String>();
            if (a.dead.isEmpty()) {
                out.add("Start with Work = Available = " + vec(a.avail) + ". Repeatedly pick an unfinished process whose Request fits inside Work, let it finish, and add its Allocation back to Work.");
                out.add("Every process could be picked, so no deadlock exists and the state is safe. One safe completion order is " + chain(a.order) + ".");
                if (!a.startFinished.isEmpty())
                    out.add(names(a.startFinished) + " hold" + (a.startFinished.size() == 1 ? "s" : "") + " no resources at the start, so they cannot be part of a deadlock and count as finished immediately.");
                return out;
            }
            int[] w = a.finalWork;
            out.add("The search stopped with Work = " + vec(w) + ". None of the " + a.dead.size() + " unfinished processes (" + names(a.dead)
                    + ") has a Request that fits inside Work, so they can never finish: they are deadlocked.");
            for (int i : a.dead) {
                StringBuilder sb = new StringBuilder("P" + i + " is stuck: ");
                boolean first = true;
                for (int r = 0; r < a.m; r++) {
                    if (a.req[i][r] <= w[r]) continue;
                    if (!first) sb.append("; ");
                    first = false;
                    sb.append("it needs ").append(a.req[i][r]).append(" of ").append(rn(r)).append(" but only ").append(w[r]).append(" can ever be free");
                    List<Integer> holders = new ArrayList<Integer>();
                    for (int j : a.dead) if (j != i && a.alloc[j][r] > 0) holders.add(j);
                    if (!holders.isEmpty()) sb.append(", and ").append(names(holders)).append(" hold").append(holders.size() == 1 ? "s" : "").append(" it");
                }
                out.add(sb.append(".").toString());
            }
            for (List<Integer> c : a.cycles) {
                List<Integer> loop = new ArrayList<Integer>(c);
                loop.add(c.get(0));
                out.add("Circular wait: " + chain(loop) + ". Each process holds something the next one is waiting for, and none of them can release it.");
            }
            if (a.cycles.isEmpty())
                out.add("At least one process asks for more than will ever be free, so it waits forever.");
            else
                out.add("All four Coffman conditions hold here: mutual exclusion, hold and wait, no preemption and circular wait.");
            if (!a.steps.isEmpty()) {
                List<Integer> ok = new ArrayList<Integer>();
                for (Step s : a.steps) ok.add(s.proc);
                out.add(names(ok) + " could still finish before the search stalled, so they are not deadlocked even though they were waiting at first.");
            }
            for (int i : a.startFinished) {
                if (!isZero(a.req[i]))
                    out.add("P" + i + " is waiting for " + vec(a.req[i]) + " too, but it holds nothing, so it is only blocked, not deadlocked.");
            }
            return out;
        }

        static List<String> explainRecovery(Analysis a, List<Option> opts) {
            List<String> out = new ArrayList<String>();
            if (opts.isEmpty()) return out;
            Option b = opts.get(0);
            out.add("Recommended: terminate " + andJoin(b.victims) + ". This is the smallest set that works (" + b.victims.size() + " process"
                    + (b.victims.size() == 1 ? "" : "es") + ") and, among such sets, the cheapest (" + b.cost + " resource instance"
                    + (b.cost == 1 ? "" : "s") + " released). Afterwards every remaining process can finish, for example in the order "
                    + chain(b.order) + ".");
            for (int i = 1; i < opts.size(); i++) {
                Option o = opts.get(i);
                out.add("Alternative: terminate " + andJoin(o.victims) + " (" + o.cost + " instance" + (o.cost == 1 ? "" : "s") + " released).");
            }
            if (!a.cycles.isEmpty() && a.waits.size() > 0) {
                int[] w = null;
                List<Integer> c = a.cycles.get(0);
                for (int[] e : a.waits) if (e[0] == c.get(0) && c.contains(e[1])) {
                    w = e;
                    break;
                }
                if (w != null)
                    out.add("Resource preemption instead of termination: take " + rn(w[2]) + " away from P" + w[1] + ", give it to P" + w[0]
                            + ", and roll P" + w[1] + " back to a checkpoint so it can request it again later. Re-run the detection afterwards, and watch for starvation if the same victim is chosen repeatedly.");
            }
            out.add("Prevention for the future: make every process request resources in one fixed global order (A, B, C ...), which makes a circular wait impossible.");
            return out;
        }
    }

    static final String[] PRESET_NAMES = {"No deadlock", "Deadlock", "Large system"};

    static final int[][][][] PRESETS = {
            
            {{{0, 1, 0}, {2, 0, 0}, {3, 0, 3}, {2, 1, 1}, {0, 0, 2}},
                    {{0, 0, 0}, {2, 0, 2}, {0, 0, 0}, {1, 0, 0}, {0, 0, 2}},
                    {{0, 0, 0}}},
            
            {{{0, 1, 0}, {2, 0, 0}, {3, 0, 3}, {2, 1, 1}, {0, 0, 2}},
                    {{0, 0, 0}, {2, 0, 2}, {0, 0, 1}, {1, 0, 0}, {0, 0, 2}},
                    {{0, 0, 0}}},

            {{{1, 0, 0, 0, 0}, {1, 0, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0},
                    {0, 0, 0, 0, 1}, {1, 0, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 0, 0, 0}},
                    {{0, 1, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 0, 1},
                            {0, 0, 1, 0, 0}, {0, 2, 0, 0, 0}, {3, 0, 0, 0, 0}, {1, 0, 0, 0, 0}},
                    {{0, 1, 0, 0, 0}}}
    };

    // Build a plain-text report using the same analysis shown in the interface.
    static final class Report {
        static String rep(char c, int n) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) sb.append(c);
            return sb.toString();
        }

        static String caseReport(String name, int[][] al, int[][] rq, int[] av) {
            Analysis a = Detector.analyze(al, rq, av);
            StringBuilder sb = new StringBuilder();
            sb.append(rep('=', 86)).append("\n TEST CASE: ").append(name).append("\n").append(rep('=', 86)).append("\n");
            sb.append(" INPUT: ").append(a.n).append(" processes, ").append(a.m).append(" resource types\n");
            sb.append("   Available: ").append(vec(a.avail)).append("   Total instances: ").append(vec(a.total)).append("\n\n");
            sb.append(String.format("   %-9s %-22s %-22s%n", "Process", "Allocation", "Request"));
            for (int i = 0; i < a.n; i++)
                sb.append(String.format("   %-9s %-22s %-22s%n", "P" + i, vec(a.alloc[i]), vec(a.req[i])));
            sb.append("\n DETECTION STEPS (Work starts as Available)\n");
            sb.append(String.format("   %-5s %-4s %-14s %-14s %-14s %s%n", "Step", "Proc", "Request", "Work before", "Work after", "What happens"));
            for (Object[] r : Detector.stepRows(a))
                sb.append(String.format("   %-5s %-4s %-14s %-14s %-14s %s%n", r[0], r[1], r[2], r[3], r[5], r[4]));
            sb.append("\n RESULT: ");
            if (a.dead.isEmpty()) sb.append("NO DEADLOCK - safe state. Safe sequence: ").append(chain(a.order)).append("\n");
            else sb.append("DEADLOCK DETECTED. Deadlocked processes: ").append(names(a.dead)).append("\n");
            sb.append("\n EXPLANATION\n");
            for (String s : Detector.explain(a)) sb.append("   * ").append(s).append("\n");
            if (!a.dead.isEmpty()) {
                sb.append("\n RECOVERY SUGGESTIONS\n");
                for (String s : Detector.explainRecovery(a, Detector.recover(a))) sb.append("   * ").append(s).append("\n");
            }
            return sb.append("\n").toString();
        }

        static String full() {
            StringBuilder sb = new StringBuilder("DEADLOCK DETECTIVE - TEST REPORT\n\n");
            for (int i = 0; i < PRESET_NAMES.length; i++)
                sb.append(caseReport(PRESET_NAMES[i], PRESETS[i][0], PRESETS[i][1], PRESETS[i][2][0]));
            return sb.toString();
        }
    }

    // Shared colors, fonts, and painting helpers for the custom Swing UI.
    static final class T {
        static final Color BG = new Color(0xF2F4F3), CARD = Color.WHITE, LINE = new Color(0xE2E6E4),
                ROW = new Color(0xF0F2F1), INK = new Color(0x101C1A), MUTED = new Color(0x667774),
                FAINT = new Color(0xA3AEAB), ACCENT = new Color(0x0B7A6B), ACCENT_HOVER = new Color(0x096A5D),
                ACCENT_SOFT = new Color(0xE3F2EF), ACCENT_MID = new Color(0xB5DCD5), TRACK = new Color(0xE8ECEA);
        static final Color[] PAL = {
                new Color(0xF4A261), new Color(0x6CA6E8), new Color(0xE76F8B), new Color(0x9B8CE0),
                new Color(0x8FCB74), new Color(0xF2CF55), new Color(0x5CC1D6), new Color(0xE89A7B),
                new Color(0xB6A38E), new Color(0xC678B8)};
        static final String FAMILY = pick();

        static String pick() {
            Set<String> have = new HashSet<String>(Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            String[] want = {"Inter", "SF Pro Text", "Segoe UI", "Helvetica Neue", "Roboto", "Noto Sans", "DejaVu Sans"};
            for (String w : want) if (have.contains(w)) return w;
            return Font.SANS_SERIF;
        }

        static Font f(int style, float size) {
            return new Font(FAMILY, Font.PLAIN, 12).deriveFont(style, size);
        }

        static Graphics2D aa(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            return g2;
        }

        static void center(Graphics2D g, String s, double cx, double cy) {
            FontMetrics fm = g.getFontMetrics();
            g.drawString(s, (float) (cx - fm.stringWidth(s) / 2.0), (float) (cy + (fm.getAscent() - fm.getDescent()) / 2.0));
        }

        static void anim(JComponent c, int ms, final DoubleConsumer step) {
            Object old = c.getClientProperty("anim");
            if (old instanceof Timer) ((Timer) old).stop();
            final long t0 = System.nanoTime();
            final Timer tm = new Timer(14, null);
            tm.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    double x = (System.nanoTime() - t0) / 1e6 / ms;
                    if (x >= 1) {
                        x = 1;
                        tm.stop();
                    }
                    step.accept(1 - Math.pow(1 - x, 3));
                }
            });
            c.putClientProperty("anim", tm);
            tm.start();
        }
    }

    static JLabel lab(String s, int style, float size, Color c) {
        JLabel l = new JLabel(s);
        l.setFont(T.f(style, size));
        l.setForeground(c);
        return l;
    }

    // Simple vertical layout that stacks visible components with a fixed gap.
    static final class VLayout implements LayoutManager {
        final int gap;

        VLayout(int gap) {
            this.gap = gap;
        }

        public void addLayoutComponent(String n, Component c) {
        }

        public void removeLayoutComponent(Component c) {
        }

        public Dimension preferredLayoutSize(Container p) {
            Insets in = p.getInsets();
            int w = 0, h = 0, k = 0;
            for (Component c : p.getComponents()) {
                if (!c.isVisible()) continue;
                Dimension d = c.getPreferredSize();
                w = Math.max(w, d.width);
                h += d.height + (k++ > 0 ? gap : 0);
            }
            return new Dimension(w + in.left + in.right, h + in.top + in.bottom);
        }

        public Dimension minimumLayoutSize(Container p) {
            return preferredLayoutSize(p);
        }

        public void layoutContainer(Container p) {
            Insets in = p.getInsets();
            int y = in.top, w = p.getWidth() - in.left - in.right;
            for (Component c : p.getComponents()) {
                if (!c.isVisible()) continue;
                int h = c.getPreferredSize().height;
                c.setBounds(in.left, y, w, h);
                y += h + gap;
            }
        }
    }

    static final class VPanel extends JPanel implements Scrollable {
        VPanel(int gap) {
            super(new VLayout(gap));
            setOpaque(false);
        }

        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
            return 24;
        }

        public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
            return 240;
        }

        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    // Rounded panel used as the base surface for dashboard cards.
    static class Card extends JPanel {
        final int arc;
        final Color fill, stroke;

        Card(int arc, Color fill, Color stroke) {
            super(new BorderLayout());
            this.arc = arc;
            this.fill = fill;
            this.stroke = stroke;
            setOpaque(false);
        }

        Card() {
            this(14, T.CARD, T.LINE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = T.aa(g);
            RoundRectangle2D.Float s = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, arc, arc);
            if (fill != null) {
                g2.setColor(fill);
                g2.fill(s);
            }
            if (stroke != null) {
                g2.setColor(stroke);
                g2.draw(s);
            }
            g2.dispose();
        }
    }

    // Button with custom colors and hover/pressed states.
    static class Btn extends JButton {
        enum Kind {PRIMARY, SOFT, GHOST}

        final Kind kind;
        boolean hover;
        int padX = 18, h = 38, fw = -1, fh = -1;

        Btn(String text, Kind kind) {
            super(text);
            this.kind = kind;
            setFont(T.f(Font.BOLD, 13));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setMargin(new Insets(0, 0, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        Btn compact() {
            padX = 12;
            h = 32;
            setFont(T.f(Font.BOLD, 12));
            return this;
        }

        Btn fixed(int w, int hh) {
            fw = w;
            fh = hh;
            return this;
        }

        @Override
        public Dimension getPreferredSize() {
            if (fw > 0) return new Dimension(fw, fh);
            return new Dimension(getFontMetrics(getFont()).stringWidth(getText()) + padX * 2, h);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = T.aa(g);
            boolean en = isEnabled(), press = getModel().isPressed();
            Color bg, fg;
            switch (kind) {
                case PRIMARY:
                    bg = !en ? T.ACCENT_MID : press ? T.ACCENT_HOVER.darker() : hover ? T.ACCENT_HOVER : T.ACCENT;
                    fg = Color.WHITE;
                    break;
                case SOFT:
                    bg = hover ? new Color(0xD3EAE5) : T.ACCENT_SOFT;
                    fg = T.ACCENT;
                    break;
                default:
                    bg = hover && en ? new Color(0xE8ECEA) : null;
                    fg = !en ? T.FAINT : hover ? T.INK : T.MUTED;
            }
            if (bg != null) {
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            }
            g2.setFont(getFont());
            g2.setColor(fg);
            T.center(g2, getText(), getWidth() / 2.0, getHeight() / 2.0);
            g2.dispose();
        }
    }

    static class Seg extends JToggleButton {
        Seg(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(T.f(Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(getFontMetrics(getFont()).stringWidth(getText()) + 36, 34);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = T.aa(g);
            if (isSelected()) {
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            }
            g2.setFont(getFont());
            g2.setColor(isSelected() ? T.INK : T.MUTED);
            T.center(g2, getText(), getWidth() / 2.0, getHeight() / 2.0);
            g2.dispose();
        }
    }

    // Bounded plus/minus control for choosing process and resource counts.
    static class Stepper extends Card {
        int value;
        final int min, max;
        Runnable onChange;
        final JLabel lbl;
        final Btn minus, plus;

        Stepper(int v, int mn, int mx) {
            super(10, T.CARD, T.LINE);
            value = v;
            min = mn;
            max = mx;
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 1));
            minus = new Btn("\u2212", Btn.Kind.GHOST).fixed(28, 30);
            plus = new Btn("+", Btn.Kind.GHOST).fixed(28, 30);
            lbl = lab("" + v, Font.BOLD, 13, T.INK);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setPreferredSize(new Dimension(26, 30));
            add(minus);
            add(lbl);
            add(plus);
            minus.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    bump(-1);
                }
            });
            plus.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    bump(1);
                }
            });
        }

        void bump(int d) {
            int nv = Math.max(min, Math.min(max, value + d));
            if (nv != value) {
                value = nv;
                lbl.setText("" + nv);
                if (onChange != null) onChange.run();
            }
        }

        void set(int v) {
            value = v;
            lbl.setText("" + v);
        }

        void setActive(boolean a) {
            minus.setEnabled(a);
            plus.setEnabled(a);
            lbl.setForeground(a ? T.INK : T.FAINT);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(86, 34);
        }
    }

    static class Pill extends JComponent {
        String text = "";

        Pill() {
            setFont(T.f(Font.BOLD, 11));
        }

        void set(String s) {
            text = s;
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(getFontMetrics(getFont()).stringWidth(text) + 22, 24);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = T.aa(g);
            g2.setColor(T.ACCENT_SOFT);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));
            g2.setColor(T.ACCENT);
            g2.setFont(getFont());
            T.center(g2, text, getWidth() / 2.0, getHeight() / 2.0);
            g2.dispose();
        }
    }

    static class SlimBar extends BasicScrollBarUI {
        private static JButton zero() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected JButton createDecreaseButton(int o) {
            return zero();
        }

        @Override
        protected JButton createIncreaseButton(int o) {
            return zero();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty()) return;
            Graphics2D g2 = T.aa(g);
            g2.setColor(isThumbRollover() ? new Color(0xB4BDBA) : new Color(0xCDD4D1));
            boolean vert = scrollbar.getOrientation() == Adjustable.VERTICAL;
            int pad = 2;
            g2.fill(new RoundRectangle2D.Float(r.x + (vert ? pad : 0), r.y + (vert ? 0 : pad),
                    r.width - (vert ? pad * 2 : 0), r.height - (vert ? 0 : pad * 2), 8, 8));
            g2.dispose();
        }
    }

    static JScrollPane scroll(Component view, Color bg) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(null);
        sp.getViewport().setBackground(bg);
        sp.setBackground(bg);
        sp.getVerticalScrollBar().setUI(new SlimBar());
        sp.getHorizontalScrollBar().setUI(new SlimBar());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        sp.getVerticalScrollBar().setUnitIncrement(18);
        return sp;
    }

    static final class Tag {
        final String id;
        final int color;

        Tag(String id, int color) {
            this.id = id;
            this.color = color;
        }

        public String toString() {
            return id;
        }
    }

    static final class Dot implements Icon {
        final Color c;

        Dot(Color c) {
            this.c = c;
        }

        public int getIconWidth() {
            return 10;
        }

        public int getIconHeight() {
            return 10;
        }

        public void paintIcon(Component comp, Graphics g, int x, int y) {
            Graphics2D g2 = T.aa(g);
            g2.setColor(c);
            g2.fill(new Ellipse2D.Float(x, y, 10, 10));
            g2.dispose();
        }
    }

    static class CellR extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, false, false, r, c);
            l.setOpaque(true);
            l.setBackground(sel ? T.ACCENT_SOFT : T.CARD);
            l.setForeground(T.INK);
            l.setFont(T.f(Font.PLAIN, 13));
            l.setHorizontalAlignment(c == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
            l.setIcon(null);
            l.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, T.ROW), new EmptyBorder(0, c == 0 ? 12 : 4, 0, c == 0 ? 12 : 4)));
            if (v instanceof Tag) {
                Tag tg = (Tag) v;
                l.setText(tg.id);
                l.setIcon(new Dot(T.PAL[tg.color % T.PAL.length]));
                l.setIconTextGap(9);
                l.setFont(T.f(Font.BOLD, 13));
            }
            post(l, t, v, r, c);
            return l;
        }

        void post(JLabel l, JTable t, Object v, int r, int c) {
        }
    }

    static class HeadR extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, false, false, r, c);
            l.setOpaque(true);
            l.setBackground(T.CARD);
            l.setForeground(T.MUTED);
            l.setFont(T.f(Font.PLAIN, 12));
            l.setHorizontalAlignment(c == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
            l.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, T.LINE), new EmptyBorder(0, c == 0 ? 12 : 4, 0, c == 0 ? 12 : 4)));
            return l;
        }
    }

    static void styleTable(JTable t, int rowH) {
        t.setRowHeight(rowH);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setBackground(T.CARD);
        t.setDefaultRenderer(Object.class, new CellR());
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setResizingAllowed(false);
        h.setDefaultRenderer(new HeadR());
        h.setPreferredSize(new Dimension(10, 34));
        h.setBackground(T.CARD);
        h.setBorder(null);
    }

    static class SimpleModel extends AbstractTableModel {
        String[] cols;
        Object[][] data;

        SimpleModel(String[] cols, Object[][] data) {
            this.cols = cols;
            this.data = data;
        }

        public int getRowCount() {
            return data.length;
        }

        public int getColumnCount() {
            return cols.length;
        }

        public String getColumnName(int c) {
            return cols[c];
        }

        public Object getValueAt(int r, int c) {
            return data[r][c];
        }
    }

    // Lightweight text component that wraps and paints explanatory paragraphs.
    static class Notes extends JComponent {
        List<String> paras = new ArrayList<String>();
        final boolean bullets;
        final Color color;
        int lastH = -1;

        Notes(boolean bullets, float size, Color color) {
            this.bullets = bullets;
            this.color = color;
            setFont(T.f(Font.PLAIN, size));
            addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    int h = measure(getWidth());
                    if (h != lastH) {
                        lastH = h;
                        revalidate();
                    }
                }
            });
        }

        void set(List<String> p) {
            paras = p;
            revalidate();
            repaint();
        }

        int indent() {
            return bullets ? 18 : 0;
        }

        List<String> wrap(FontMetrics fm, String text, int width) {
            List<String> lines = new ArrayList<String>();
            StringBuilder cur = new StringBuilder();
            for (String w : text.split(" ")) {
                String test = cur.length() == 0 ? w : cur + " " + w;
                if (fm.stringWidth(test) > width && cur.length() > 0) {
                    lines.add(cur.toString());
                    cur = new StringBuilder(w);
                } else cur = new StringBuilder(test);
            }
            if (cur.length() > 0) lines.add(cur.toString());
            return lines;
        }

        int measure(int width) {
            FontMetrics fm = getFontMetrics(getFont());
            int lh = fm.getHeight() + 3, h = 0;
            for (String p : paras) h += wrap(fm, p, Math.max(80, width - indent())).size() * lh + 9;
            return Math.max(h - (paras.isEmpty() ? 0 : 9), 4);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(200, measure(getWidth() > 0 ? getWidth() : 700));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = T.aa(g);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int lh = fm.getHeight() + 3, y = fm.getAscent();
            for (String p : paras) {
                if (bullets) {
                    g2.setColor(T.ACCENT);
                    g2.fill(new Ellipse2D.Float(3, y - fm.getAscent() / 2f - 2, 5, 5));
                }
                g2.setColor(color);
                for (String line : wrap(fm, p, Math.max(80, getWidth() - indent()))) {
                    g2.drawString(line, indent(), y);
                    y += lh;
                }
                y += 9;
            }
            g2.dispose();
        }
    }

    static class LogoD extends JComponent {
        LogoD() {
            setPreferredSize(new Dimension(42, 42));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = T.aa(g);
            g2.setPaint(new GradientPaint(0, 0, new Color(0x13A18C), 42, 42, new Color(0x08574D)));
            g2.fill(new RoundRectangle2D.Float(0, 0, 42, 42, 12, 12));
            g2.setColor(new Color(255, 255, 255, 150));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Line2D.Float(14, 14, 29, 14));
            g2.draw(new Line2D.Float(30, 17, 22, 30));
            g2.draw(new Line2D.Float(19, 29, 13, 17));
            g2.setColor(new Color(255, 255, 255, 240));
            g2.fill(new Ellipse2D.Float(8, 8, 11, 11));
            g2.fill(new Ellipse2D.Float(24, 8, 11, 11));
            g2.setColor(new Color(0xF4A261));
            g2.fill(new Ellipse2D.Float(16, 25, 11, 11));
            g2.dispose();
        }
    }

    static class Banner extends JPanel {
        Color fill = T.ACCENT_SOFT, stroke = T.ACCENT_MID;

        Banner() {
            super(new VLayout(6));
            setOpaque(false);
            setBorder(new EmptyBorder(18, 22, 18, 22));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = T.aa(g);
            RoundRectangle2D.Float s = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 14, 14);
            g2.setColor(fill);
            g2.fill(s);
            g2.setColor(stroke);
            g2.draw(s);
            g2.dispose();
        }
    }

    // Interactive resource-allocation graph; nodes can be dragged to reposition them.
    static class RAG extends JPanel {
        static final int PR = 24;                        
        static final Color HOT = new Color(0xD64545);
        Analysis an;
        double[][] pp = new double[0][], rp = new double[0][];    
        double ax, ay, aw = 1, ah = 1, pulse;
        int dragT = -1, dragI = -1;

        RAG() {
            setOpaque(false);
            MouseAdapter ma = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    int[] f = find(e.getX(), e.getY());
                    if (f != null) {
                        dragT = f[0];
                        dragI = f[1];
                    }
                }

                public void mouseDragged(MouseEvent e) {
                    if (dragT < 0) return;
                    double[] f = dragT == 0 ? pp[dragI] : rp[dragI];
                    f[0] = Math.max(0, Math.min(1, (e.getX() - ax) / aw));
                    f[1] = Math.max(0, Math.min(1, (e.getY() - ay) / ah));
                    repaint();
                }

                public void mouseReleased(MouseEvent e) {
                    dragT = -1;
                }

                public void mouseMoved(MouseEvent e) {
                    setCursor(Cursor.getPredefinedCursor(find(e.getX(), e.getY()) != null ? Cursor.MOVE_CURSOR : Cursor.DEFAULT_CURSOR));
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
            new Timer(60, new ActionListener() {            
                public void actionPerformed(ActionEvent e) {
                    pulse = (System.nanoTime() / 1e9 % 1.6) / 1.6;
                    if (an != null && !an.dead.isEmpty() && isShowing()) repaint();
                }
            }).start();
        }

        void setData(Analysis a) {
            an = a;
            int n = a.n, m = a.m;
            pp = new double[n][2];
            rp = new double[m][2];
            
            List<Integer> ord = new ArrayList<Integer>();
            boolean[] seen = new boolean[n + m];
            for (int i : a.dead) walk(i, seen, ord);
            for (int k = 0; k < n + m; k++) walk(k, seen, ord);
            for (int k = 0; k < ord.size(); k++) {
                double th = -Math.PI / 2 + 2 * Math.PI * k / ord.size();
                double[] f = {0.5 + 0.5 * Math.cos(th), 0.5 + 0.5 * Math.sin(th)};
                int u = ord.get(k);
                if (u < n) pp[u] = f;
                else rp[u - n] = f;
            }
            repaint();
        }

        void walk(int u, boolean[] seen, List<Integer> ord) {
            if (seen[u]) return;
            seen[u] = true;
            ord.add(u);
            if (u < an.n) {
                for (int r = 0; r < an.m; r++) if (an.alloc[u][r] > 0 || an.req[u][r] > 0) walk(an.n + r, seen, ord);
            } else {
                int r = u - an.n;
                for (int i = 0; i < an.n; i++) if (an.alloc[i][r] > 0 || an.req[i][r] > 0) walk(i, seen, ord);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(600, 580);
        }

        double[] node(int t, int i) {
            if (t == 0) return new double[]{ax + pp[i][0] * aw, ay + pp[i][1] * ah, 1, PR, PR};
            int dots = Math.min(an.total[i], 8);
            double w = Math.max(60, dots * 11 + 16);
            return new double[]{ax + rp[i][0] * aw, ay + rp[i][1] * ah, 0, w / 2, 21};
        }

        int[] find(int x, int y) {
            if (an == null) return null;
            for (int i = 0; i < an.n; i++) {
                double[] nd = node(0, i);
                if (Math.hypot(x - nd[0], y - nd[1]) <= PR) return new int[]{0, i};
            }
            for (int j = 0; j < an.m; j++) {
                double[] nd = node(1, j);
                if (Math.abs(x - nd[0]) <= nd[3] && Math.abs(y - nd[1]) <= nd[4]) return new int[]{1, j};
            }
            return null;
        }

        static double[] clip(double[] nd, double tx, double ty) {
            double dx = tx - nd[0], dy = ty - nd[1], len = Math.hypot(dx, dy);
            if (len < 1e-9) return new double[]{nd[0], nd[1]};
            if (nd[2] == 1) return new double[]{nd[0] + dx / len * nd[3], nd[1] + dy / len * nd[3]};
            double t = Math.min(Math.abs(dx) < 1e-9 ? 1e18 : nd[3] / Math.abs(dx), Math.abs(dy) < 1e-9 ? 1e18 : nd[4] / Math.abs(dy));
            return new double[]{nd[0] + dx * t, nd[1] + dy * t};
        }

        void edge(Graphics2D g, double[] from, double[] to, Color col, boolean dashed, int count) {
            double dx = to[0] - from[0], dy = to[1] - from[1], len = Math.hypot(dx, dy);
            if (len < 1) return;
            double cx = (from[0] + to[0]) / 2 - dy / len * 24, cy = (from[1] + to[1]) / 2 + dx / len * 24;   
            double[] s = clip(from, cx, cy), e = clip(to, cx, cy);
            Path2D.Double p = new Path2D.Double();
            p.moveTo(s[0], s[1]);
            p.quadTo(cx, cy, e[0], e[1]);
            g.setColor(col);
            g.setStroke(dashed ? new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{6f, 4f}, 0f)
                    : new BasicStroke(1.9f));
            g.draw(p);
            double ux = e[0] - cx, uy = e[1] - cy, ul = Math.hypot(ux, uy);
            ux /= ul;
            uy /= ul;
            Path2D.Double ar = new Path2D.Double();
            ar.moveTo(e[0], e[1]);
            ar.lineTo(e[0] - ux * 11 - uy * 5, e[1] - uy * 11 + ux * 5);
            ar.lineTo(e[0] - ux * 11 + uy * 5, e[1] - uy * 11 - ux * 5);
            ar.closePath();
            g.setStroke(new BasicStroke(1f));
            g.fill(ar);
            if (count > 1) {
                double lx = 0.25 * s[0] + 0.5 * cx + 0.25 * e[0], ly = 0.25 * s[1] + 0.5 * cy + 0.25 * e[1];
                g.setFont(T.f(Font.BOLD, 11));
                String t = "x" + count;
                int w = g.getFontMetrics().stringWidth(t) + 10;
                g.setColor(Color.WHITE);
                g.fill(new RoundRectangle2D.Double(lx - w / 2.0, ly - 9, w, 18, 18, 18));
                g.setColor(col);
                g.draw(new RoundRectangle2D.Double(lx - w / 2.0, ly - 9, w, 18, 18, 18));
                T.center(g, t, lx, ly);
            }
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = T.aa(g0);
            if (an == null || an.n == 0) {
                g.setFont(T.f(Font.PLAIN, 13));
                g.setColor(T.MUTED);
                T.center(g, "Add a process to see the graph", getWidth() / 2.0, getHeight() / 2.0);
                g.dispose();
                return;
            }
            ax = 70;
            ay = 44;
            aw = Math.max(50, getWidth() - 140);
            ah = Math.max(50, getHeight() - 140);
            boolean[] dead = new boolean[an.n];
            for (int i : an.dead) dead[i] = true;
            int[] held = new int[an.m];
            for (int[] row : an.alloc) for (int r = 0; r < an.m; r++) held[r] += row[r];

            Color assign = new Color(0x55655F);
            for (int i = 0; i < an.n; i++) {
                for (int r = 0; r < an.m; r++) {
                    if (an.alloc[i][r] > 0) {
                        boolean hot = false;                    
                        if (dead[i]) for (int k : an.dead) if (k != i && an.req[k][r] > an.finalWork[r]) hot = true;
                        edge(g, node(1, r), node(0, i), hot ? HOT : assign, false, an.alloc[i][r]);
                    }
                    if (an.req[i][r] > 0) {
                        boolean hot = dead[i] && an.req[i][r] > an.finalWork[r];
                        edge(g, node(0, i), node(1, r), hot ? HOT : T.ACCENT, true, an.req[i][r]);
                    }
                }
            }

            for (int r = 0; r < an.m; r++) {
                double[] nd = node(1, r);
                RoundRectangle2D.Double b = new RoundRectangle2D.Double(nd[0] - nd[3], nd[1] - nd[4], nd[3] * 2, nd[4] * 2, 10, 10);
                g.setColor(Color.WHITE);
                g.fill(b);
                g.setColor(new Color(0xA9B6B2));
                g.setStroke(new BasicStroke(1.4f));
                g.draw(b);
                g.setFont(T.f(Font.BOLD, 12));
                g.setColor(T.INK);
                g.drawString(rn(r), (float) (nd[0] - nd[3] + 9), (float) (nd[1] - nd[4] + 16));
                int dots = Math.min(an.total[r], 8);
                for (int k = 0; k < dots; k++) {
                    double dx = nd[0] - nd[3] + 12 + k * 11, dy = nd[1] + nd[4] - 12;
                    Ellipse2D.Double d = new Ellipse2D.Double(dx - 4, dy - 4, 8, 8);
                    if (k < held[r]) {
                        g.setColor(T.INK);
                        g.fill(d);
                    } else {
                        g.setColor(T.ACCENT);
                        g.setStroke(new BasicStroke(1.4f));
                        g.draw(d);
                    }
                }
            }

            for (int i = 0; i < an.n; i++) {
                double[] nd = node(0, i);
                if (dead[i]) {
                    g.setColor(new Color(HOT.getRed(), HOT.getGreen(), HOT.getBlue(), (int) (110 * (1 - pulse))));
                    double rr = PR + 5 + pulse * 9;
                    g.fill(new Ellipse2D.Double(nd[0] - rr, nd[1] - rr, rr * 2, rr * 2));
                }
                g.setColor(T.PAL[i % T.PAL.length]);
                g.fill(new Ellipse2D.Double(nd[0] - PR, nd[1] - PR, PR * 2, PR * 2));
                g.setStroke(new BasicStroke(dead[i] ? 3f : 2f));
                g.setColor(dead[i] ? HOT : Color.WHITE);
                g.draw(new Ellipse2D.Double(nd[0] - PR, nd[1] - PR, PR * 2, PR * 2));
                g.setFont(T.f(Font.BOLD, 14));
                g.setColor(T.INK);
                T.center(g, "P" + i, nd[0], nd[1]);
            }

            int ly = getHeight() - 26;
            g.setFont(T.f(Font.PLAIN, 12));
            int x = 24;
            x = legend(g, x, ly, assign, false, "Resource assigned to process");
            x = legend(g, x, ly, T.ACCENT, true, "Process requests resource");
            x = legend(g, x, ly, HOT, false, "Involved in the deadlock");
            g.setColor(T.INK);
            g.fill(new Ellipse2D.Double(x, ly - 4, 8, 8));
            g.setColor(T.MUTED);
            g.drawString("In use", x + 14, ly + 4);
            g.setColor(T.ACCENT);
            g.setStroke(new BasicStroke(1.4f));
            g.draw(new Ellipse2D.Double(x + 62, ly - 4, 8, 8));
            g.setColor(T.MUTED);
            g.drawString("Free instance", x + 76, ly + 4);
            g.dispose();
        }

        int legend(Graphics2D g, int x, int y, Color c, boolean dashed, String text) {
            g.setColor(c);
            g.setStroke(dashed ? new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{4f, 3f}, 0f)
                    : new BasicStroke(1.9f));
            g.draw(new Line2D.Double(x, y, x + 24, y));
            g.setStroke(new BasicStroke(1f));
            g.setColor(T.MUTED);
            g.drawString(text, x + 32, y + 4);
            return x + 32 + g.getFontMetrics().stringWidth(text) + 22;
        }
    }

    final JPanel root = new JPanel(new BorderLayout());
    int n = 5, m = 3;
    int[][] alloc = new int[5][3], req = new int[5][3];
    int[] avail = new int[3];
    String caseName = "";
    boolean showAlloc = true;
    Analysis an;
    List<Option> opts = new ArrayList<Option>();
    final Random rnd = new Random();

    MatModel matModel;
    AvailModel availModel;
    JTable matTable, availTable;
    JLabel caseLabel;
    Stepper procStep, resStep;
    Seg segAlloc, segReq;

    Banner banner;
    JLabel vKicker, vTitle;
    Notes vText, explainNotes, recoverNotes;
    JLabel[] statVals = new JLabel[5];
    JTable snapTable, stepTable;
    JLabel availLine;
    Card recoverCard;
    RAG rag = new RAG();

    JPanel pages;
    VPanel detPage, graphPage;
    Component currentPage;
    Seg segDet, segGraph;
    JLabel status;

    // Assemble the main window and connect the sidebar to the workspace.
    DeadlockDetective() {
        root.setBackground(T.BG);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMain(), BorderLayout.CENTER);
        loadPreset(1);
        refresh();
    }

    void show() {
        JFrame f = new JFrame("Deadlock Detective");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setContentPane(root);
        Dimension sc = Toolkit.getDefaultToolkit().getScreenSize();
        f.setSize(Math.min(1400, sc.width - 60), Math.min(900, sc.height - 80));
        f.setMinimumSize(new Dimension(1100, 680));
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    class MatModel extends AbstractTableModel {
        public int getRowCount() {
            return n;
        }

        public int getColumnCount() {
            return m + 1;
        }

        public String getColumnName(int c) {
            return c == 0 ? "Process" : rn(c - 1);
        }

        public Object getValueAt(int r, int c) {
            if (c == 0) return new Tag("P" + r, r);
            return (showAlloc ? alloc : req)[r][c - 1];
        }

        public boolean isCellEditable(int r, int c) {
            return c >= 1;
        }

        public void setValueAt(Object v, int r, int c) {
            int x = parse(v);
            if (x < 0) return;
            (showAlloc ? alloc : req)[r][c - 1] = x;
            fireTableCellUpdated(r, c);
            onEdited();
        }
    }

    class AvailModel extends AbstractTableModel {
        public int getRowCount() {
            return 1;
        }

        public int getColumnCount() {
            return m;
        }

        public String getColumnName(int c) {
            return rn(c);
        }

        public Object getValueAt(int r, int c) {
            return avail[c];
        }

        public boolean isCellEditable(int r, int c) {
            return true;
        }

        public void setValueAt(Object v, int r, int c) {
            int x = parse(v);
            if (x < 0) return;
            avail[c] = x;
            fireTableCellUpdated(r, c);
            onEdited();
        }
    }

    static int parse(Object v) {
        try {
            return Math.max(0, Math.min(99, Integer.parseInt(String.valueOf(v).trim())));
        } catch (Exception e) {
            return -1;
        }
    }

    DefaultCellEditor makeEditor() {
        final JTextField tf = new JTextField();
        tf.setHorizontalAlignment(SwingConstants.CENTER);
        tf.setFont(T.f(Font.BOLD, 13));
        tf.setBorder(new MatteBorder(0, 0, 2, 0, T.ACCENT));
        DefaultCellEditor ed = new DefaultCellEditor(tf) {
            @Override
            public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
                Component comp = super.getTableCellEditorComponent(t, v, s, r, c);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        tf.selectAll();
                    }
                });
                return comp;
            }
        };
        ed.setClickCountToStart(1);
        return ed;
    }

    static class NumR extends CellR {
        @Override
        void post(JLabel l, JTable t, Object v, int r, int c) {
            if (!(v instanceof Tag)) l.setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    void setupCols() {
        TableColumnModel cm = matTable.getColumnModel();
        cm.getColumn(0).setPreferredWidth(78);
        for (int c = 1; c < cm.getColumnCount(); c++) cm.getColumn(c).setPreferredWidth(46);
    }

    // Create controls for presets, system dimensions, and editing allocation/request data.
    JComponent buildSidebar() {
        VPanel side = new VPanel(14);
        side.setBorder(new EmptyBorder(24, 22, 22, 12));

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.add(new LogoD(), BorderLayout.WEST);
        VPanel bt = new VPanel(1);
        bt.add(lab("Deadlock Detective", Font.BOLD, 18, T.INK));
        bt.add(lab("Detect, explain and recover", Font.PLAIN, 12, T.MUTED));
        brand.add(bt, BorderLayout.CENTER);
        side.add(brand);

        Card cc = new Card();
        cc.setLayout(new VLayout(10));
        cc.setBorder(new EmptyBorder(16, 16, 16, 16));
        JPanel ch = new JPanel(new BorderLayout());
        ch.setOpaque(false);
        ch.add(lab("Test case", Font.BOLD, 14, T.INK), BorderLayout.WEST);
        caseLabel = lab("", Font.PLAIN, 12, T.MUTED);
        ch.add(caseLabel, BorderLayout.EAST);
        cc.add(ch);
        JPanel presets = new JPanel(new GridLayout(1, 3, 6, 0));
        presets.setOpaque(false);
        for (int i = 0; i < PRESET_NAMES.length; i++) {
            final int idx = i;
            Btn b = new Btn(PRESET_NAMES[i], Btn.Kind.SOFT).compact();
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    loadPreset(idx);
                    refresh();
                }
            });
            presets.add(b);
        }
        cc.add(presets);
        procStep = new Stepper(5, 1, 12);
        resStep = new Stepper(3, 1, 6);
        Runnable resize = new Runnable() {
            public void run() {
                resize(procStep.value, resStep.value);
            }
        };
        procStep.onChange = resize;
        resStep.onChange = resize;
        JPanel params = new JPanel(new GridLayout(1, 2, 12, 0));
        params.setOpaque(false);
        params.add(param("Processes", procStep));
        params.add(param("Resources", resStep));
        cc.add(params);
        JPanel acts = new JPanel(new GridLayout(1, 2, 6, 0));
        acts.setOpaque(false);
        Btn rand = new Btn("Random system", Btn.Kind.SOFT).compact();
        Btn clear = new Btn("Clear matrices", Btn.Kind.GHOST).compact();
        rand.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                randomSystem();
            }
        });
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopEditing();
                alloc = new int[n][m];
                req = new int[n][m];
                avail = new int[m];
                refreshTables();
                onEdited();
            }
        });
        acts.add(rand);
        acts.add(clear);
        cc.add(acts);
        side.add(cc);

        Card mc = new Card();
        mc.setLayout(new VLayout(10));
        mc.setBorder(new EmptyBorder(16, 16, 16, 16));
        JPanel mh = new JPanel(new BorderLayout());
        mh.setOpaque(false);
        mh.add(lab("Matrices", Font.BOLD, 14, T.INK), BorderLayout.WEST);
        mh.add(lab("Click a cell to edit", Font.PLAIN, 12, T.MUTED), BorderLayout.EAST);
        mc.add(mh);

        Card segWrap = new Card(14, new Color(0xE4E9E7), null);
        segWrap.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
        segAlloc = new Seg("Allocation");
        segReq = new Seg("Request");
        ButtonGroup g = new ButtonGroup();
        g.add(segAlloc);
        g.add(segReq);
        segAlloc.setSelected(true);
        segWrap.add(segAlloc);
        segWrap.add(segReq);
        segAlloc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopEditing();
                showAlloc = true;
                matModel.fireTableDataChanged();
            }
        });
        segReq.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopEditing();
                showAlloc = false;
                matModel.fireTableDataChanged();
            }
        });
        JPanel segHold = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        segHold.setOpaque(false);
        segHold.add(segWrap);
        mc.add(segHold);

        matModel = new MatModel();
        matTable = new JTable(matModel);
        styleTable(matTable, 34);
        matTable.setDefaultRenderer(Object.class, new NumR());
        matTable.setSelectionBackground(T.ACCENT_SOFT);
        matTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        matTable.setDefaultEditor(Object.class, makeEditor());
        JScrollPane tsp = scroll(matTable, T.CARD);
        tsp.setPreferredSize(new Dimension(10, 34 + 34 * 5 + 2));
        mc.add(tsp);

        mc.add(lab("Available (free instances)", Font.PLAIN, 12, T.MUTED));
        availModel = new AvailModel();
        availTable = new JTable(availModel);
        styleTable(availTable, 34);
        availTable.setDefaultRenderer(Object.class, new NumR());
        availTable.getTableHeader().setDefaultRenderer(new HeadR() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sl, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sl, f, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                return l;
            }
        });
        availTable.setSelectionBackground(T.ACCENT_SOFT);
        availTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        availTable.setDefaultEditor(Object.class, makeEditor());
        JPanel av = new JPanel(new VLayout(0));
        av.setOpaque(false);
        av.add(availTable.getTableHeader());
        av.add(availTable);
        mc.add(av);
        side.add(mc);

        Btn run = new Btn("Detect deadlock", Btn.Kind.PRIMARY);
        run.setFont(T.f(Font.BOLD, 14));
        run.h = 46;
        run.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopEditing();
                refresh();
                showPage(false);
            }
        });
        side.add(run);

        JScrollPane sp = scroll(side, T.BG);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setPreferredSize(new Dimension(398, 100));
        return sp;
    }

    JPanel param(String name, Stepper s) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        p.add(lab(name, Font.PLAIN, 12, T.MUTED), BorderLayout.WEST);
        p.add(s, BorderLayout.EAST);
        return p;
    }

    void stopEditing() {
        if (matTable != null && matTable.isEditing()) matTable.getCellEditor().cancelCellEditing();
        if (availTable != null && availTable.isEditing()) availTable.getCellEditor().cancelCellEditing();
    }

    void refreshTables() {
        stopEditing();
        matModel.fireTableStructureChanged();
        availModel.fireTableStructureChanged();
        setupCols();
    }

    JComponent buildMain() {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(24, 6, 0, 12));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        Card segWrap = new Card(14, new Color(0xE4E9E7), null);
        segWrap.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
        segDet = new Seg("Detection");
        segGraph = new Seg("Resource graph");
        ButtonGroup g = new ButtonGroup();
        g.add(segDet);
        g.add(segGraph);
        segDet.setSelected(true);
        segWrap.add(segDet);
        segWrap.add(segGraph);
        segDet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPage(false);
            }
        });
        segGraph.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPage(true);
            }
        });
        JPanel segHold = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        segHold.setOpaque(false);
        segHold.add(segWrap);
        top.add(segHold, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        right.setOpaque(false);
        status = lab("", Font.PLAIN, 12, T.ACCENT);
        Btn png = new Btn("Save snapshot", Btn.Kind.SOFT).compact();
        Btn rep = new Btn("Export report", Btn.Kind.SOFT).compact();
        png.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveSnapshot();
            }
        });
        rep.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exportReport();
            }
        });
        right.add(status);
        right.add(png);
        right.add(rep);
        top.add(right, BorderLayout.EAST);
        main.add(top, BorderLayout.NORTH);

        pages = new JPanel(new CardLayout());
        pages.setOpaque(false);
        detPage = buildDetectionPage();
        graphPage = buildGraphPage();
        pages.add(scroll(detPage, T.BG), "det");
        pages.add(scroll(graphPage, T.BG), "graph");
        currentPage = detPage;
        main.add(pages, BorderLayout.CENTER);
        return main;
    }

    void showPage(boolean graph) {
        ((CardLayout) pages.getLayout()).show(pages, graph ? "graph" : "det");
        currentPage = graph ? graphPage : detPage;
        (graph ? segGraph : segDet).setSelected(true);
    }

    // Show the current analysis, step-by-step table, explanation, and recovery advice.
    VPanel buildDetectionPage() {
        VPanel p = new VPanel(16);
        p.setBorder(new EmptyBorder(0, 0, 24, 12));

        banner = new Banner();
        vKicker = lab("Verdict", Font.BOLD, 13, T.ACCENT);
        vTitle = lab("", Font.BOLD, 28, T.INK);
        vText = new Notes(false, 14, T.INK);
        banner.add(vKicker);
        banner.add(vTitle);
        banner.add(vText);
        p.add(banner);

        Card strip = new Card();
        strip.setLayout(new GridLayout(1, 5));
        String[] names = {"Processes", "Resource types", "Finished", "Deadlocked", "Detection steps"};
        for (int i = 0; i < 5; i++) {
            final boolean first = i == 0;
            JPanel cell = new JPanel(new VLayout(3)) {
                @Override
                protected void paintComponent(Graphics gr) {
                    super.paintComponent(gr);
                    if (!first) {
                        gr.setColor(T.LINE);
                        gr.fillRect(0, 16, 1, getHeight() - 32);
                    }
                }
            };
            cell.setOpaque(false);
            cell.setBorder(new EmptyBorder(16, 20, 16, 8));
            cell.add(lab(names[i], Font.PLAIN, 12, T.MUTED));
            statVals[i] = lab("-", Font.BOLD, 24, T.INK);
            cell.add(statVals[i]);
            strip.add(cell);
        }
        p.add(strip);

        Card sc = new Card();
        sc.setLayout(new VLayout(0));
        sc.setBorder(new EmptyBorder(16, 6, 12, 6));
        JPanel sh = new JPanel(new BorderLayout());
        sh.setOpaque(false);
        sh.setBorder(new EmptyBorder(0, 12, 10, 12));
        sh.add(lab("Input matrices", Font.BOLD, 14, T.INK), BorderLayout.WEST);
        availLine = lab("", Font.PLAIN, 12, T.MUTED);
        sh.add(availLine, BorderLayout.EAST);
        sc.add(sh);
        snapTable = new JTable(new SimpleModel(new String[0], new Object[0][0]));
        styleTable(snapTable, 34);
        snapTable.setRowSelectionAllowed(false);
        snapTable.setFocusable(false);
        sc.add(snapTable.getTableHeader());
        sc.add(snapTable);
        p.add(sc);

        Card tc = new Card();
        tc.setLayout(new VLayout(0));
        tc.setBorder(new EmptyBorder(16, 6, 12, 6));
        JPanel th = new JPanel(new BorderLayout());
        th.setOpaque(false);
        th.setBorder(new EmptyBorder(0, 12, 10, 12));
        th.add(lab("Detection steps", Font.BOLD, 14, T.INK), BorderLayout.WEST);
        th.add(lab("Work starts as Available and grows as processes finish", Font.PLAIN, 12, T.MUTED), BorderLayout.EAST);
        tc.add(th);
        stepTable = new JTable(new SimpleModel(new String[0], new Object[0][0]));
        styleTable(stepTable, 36);
        stepTable.setRowSelectionAllowed(false);
        stepTable.setFocusable(false);
        tc.add(stepTable.getTableHeader());
        tc.add(stepTable);
        p.add(tc);

        Card ec = new Card();
        ec.setLayout(new VLayout(10));
        ec.setBorder(new EmptyBorder(16, 22, 18, 22));
        ec.add(lab("Why this verdict", Font.BOLD, 14, T.INK));
        explainNotes = new Notes(true, 13, T.INK);
        ec.add(explainNotes);
        p.add(ec);

        recoverCard = new Card();
        recoverCard.setLayout(new VLayout(10));
        recoverCard.setBorder(new EmptyBorder(16, 22, 18, 22));
        recoverCard.add(lab("Recovery suggestions", Font.BOLD, 14, T.INK));
        recoverNotes = new Notes(true, 13, T.INK);
        recoverCard.add(recoverNotes);
        p.add(recoverCard);
        return p;
    }

    // Present the resource-allocation graph and its legend.
    VPanel buildGraphPage() {
        VPanel p = new VPanel(16);
        p.setBorder(new EmptyBorder(0, 0, 24, 12));
        Card gc = new Card();
        gc.setLayout(new VLayout(4));
        gc.setBorder(new EmptyBorder(16, 18, 8, 18));
        gc.add(lab("Resource-allocation graph", Font.BOLD, 14, T.INK));
        gc.add(lab("Drag any node to rearrange the picture. Deadlocked processes pulse red.", Font.PLAIN, 12, T.MUTED));
        gc.add(rag);
        p.add(gc);
        return p;
    }

    // Replace the current inputs with one of the built-in example systems.
    void loadPreset(int i) {
        stopEditing();
        int[][][] P = PRESETS[i];
        n = P[0].length;
        m = P[2][0].length;
        alloc = deep(P[0]);
        req = deep(P[1]);
        avail = P[2][0].clone();
        caseName = PRESET_NAMES[i];
        procStep.set(n);
        resStep.set(m);
        caseLabel.setText(caseName);
        refreshTables();
    }

    void resize(int nn, int mm) {
        stopEditing();
        int[][] a2 = new int[nn][mm], r2 = new int[nn][mm];
        int[] v2 = new int[mm];
        for (int i = 0; i < Math.min(n, nn); i++)
            for (int j = 0; j < Math.min(m, mm); j++) {
                a2[i][j] = alloc[i][j];
                r2[i][j] = req[i][j];
            }
        for (int j = 0; j < Math.min(m, mm); j++) v2[j] = avail[j];
        n = nn;
        m = mm;
        alloc = a2;
        req = r2;
        avail = v2;
        refreshTables();
        onEdited();
    }

    // Generate a sample allocation/request system for experimentation.
    void randomSystem() {
        stopEditing();
        boolean wantDead = rnd.nextBoolean();
        for (int attempt = 0; attempt < 80; attempt++) {
            int nn = 4 + rnd.nextInt(5), mm = 2 + rnd.nextInt(3);
            int[][] al = new int[nn][mm], rq = new int[nn][mm];
            int[] av = new int[mm];
            for (int r = 0; r < mm; r++) {
                int tot = 1 + rnd.nextInt(3);
                for (int k = 0; k < tot; k++) al[rnd.nextInt(nn)][r]++;
                av[r] = rnd.nextInt(2);
            }
            for (int i = 0; i < nn; i++)
                for (int r = 0; r < mm; r++) if (rnd.nextInt(3) == 0) rq[i][r] = 1 + rnd.nextInt(2);
            n = nn;
            m = mm;
            alloc = al;
            req = rq;
            avail = av;
            if (Detector.analyze(al, rq, av).dead.isEmpty() != wantDead) break;
        }
        procStep.set(n);
        resStep.set(m);
        caseName = "Random";
        caseLabel.setText(caseName);
        refreshTables();
        refresh();
    }

    void onEdited() {
        caseName = "Custom";
        caseLabel.setText(caseName);
        refresh();
    }

    // Re-run detection and refresh every view that depends on the current inputs.
    void refresh() {
        an = Detector.analyze(alloc, req, avail);
        opts = an.dead.isEmpty() ? new ArrayList<Option>() : Detector.recover(an);
        boolean ok = an.dead.isEmpty();

        banner.fill = ok ? T.ACCENT_SOFT : new Color(0xFDEEEE);
        banner.stroke = ok ? T.ACCENT_MID : new Color(0xF2B8B8);
        vKicker.setForeground(ok ? T.ACCENT : new Color(0xB42318));
        vTitle.setText(ok ? "No deadlock: safe state" : "Deadlock detected");
        vText.set(Collections.singletonList(ok
                ? "Every process can finish. Safe sequence: " + chain(an.order) + "."
                : "Deadlocked processes: " + names(an.dead) + " (" + an.dead.size() + " of " + an.n + "). They wait for each other forever."));
        banner.repaint();

        statVals[0].setText(String.valueOf(an.n));
        statVals[1].setText(String.valueOf(an.m));
        statVals[2].setText(String.valueOf(an.n - an.dead.size()));
        statVals[3].setText(String.valueOf(an.dead.size()));
        statVals[3].setForeground(ok ? T.INK : new Color(0xB42318));
        statVals[4].setText(String.valueOf(an.steps.size()));

        final boolean[] dead = new boolean[an.n];
        for (int i : an.dead) dead[i] = true;
        String[] cols = new String[1 + 2 * an.m];
        cols[0] = "Process";
        for (int r = 0; r < an.m; r++) {
            cols[1 + r] = "Allocation " + rn(r);
            cols[1 + an.m + r] = "Request " + rn(r);
        }
        Object[][] d = new Object[an.n][cols.length];
        for (int i = 0; i < an.n; i++) {
            d[i][0] = new Tag("P" + i, i);
            for (int r = 0; r < an.m; r++) {
                d[i][1 + r] = an.alloc[i][r];
                d[i][1 + an.m + r] = an.req[i][r];
            }
        }
        snapTable.setModel(new SimpleModel(cols, d));
        final int mm = an.m;
        snapTable.setDefaultRenderer(Object.class, new CellR() {
            @Override
            void post(JLabel l, JTable t, Object v, int r, int c) {
                if (r < dead.length && dead[r]) {
                    l.setBackground(new Color(0xFEF3F3));
                    if (c == 0) l.setForeground(new Color(0xB42318));
                }
                if (c > mm) l.setForeground(c > 0 && r < dead.length && dead[r] ? new Color(0xB42318) : T.MUTED);
            }
        });
        availLine.setText("Available " + vec(an.avail) + "   Total instances " + vec(an.total));

        List<Object[]> rows = Detector.stepRows(an);
        Object[][] sd = rows.toArray(new Object[0][]);
        final int firstBlocked = rows.size() - an.dead.size();
        stepTable.setModel(new SimpleModel(new String[]{"Step", "Process", "Request", "Work before", "What happens", "Work after"}, sd));
        TableColumnModel cm = stepTable.getColumnModel();
        int[] w = {60, 90, 120, 120, 430, 120};
        for (int c = 0; c < 6; c++) cm.getColumn(c).setPreferredWidth(w[c]);
        stepTable.setDefaultRenderer(Object.class, new CellR() {
            @Override
            void post(JLabel l, JTable t, Object v, int r, int c) {
                l.setHorizontalAlignment(c == 4 ? SwingConstants.LEFT : c == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
                if (c != 1 && c != 0) l.setForeground(T.MUTED);
                if (r >= firstBlocked) {
                    l.setBackground(new Color(0xFEF3F3));
                    if (c == 4 || c == 0) {
                        l.setForeground(new Color(0xB42318));
                        l.setFont(T.f(Font.BOLD, 13));
                    }
                } else if (c == 4) l.setForeground(T.INK);
            }
        });

        explainNotes.set(Detector.explain(an));
        recoverCard.setVisible(!ok);
        recoverNotes.set(Detector.explainRecovery(an, opts));

        rag.setData(an);
        root.revalidate();
        root.repaint();
    }

    void flash(String msg) {
        status.setText(msg);
        Timer t = new Timer(3500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                status.setText("");
            }
        });
        t.setRepeats(false);
        t.start();
    }

    File choose(String name) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(name));
        if (fc.showSaveDialog(root) != JFileChooser.APPROVE_OPTION) return null;
        return fc.getSelectedFile();
    }

    // Export the currently visible page as a high-resolution PNG image.
    void saveSnapshot() {
        File f = choose(currentPage == graphPage ? "resource_graph.png" : "detection.png");
        if (f == null) return;
        try {
            Component v = currentPage;
            BufferedImage img = new BufferedImage(v.getWidth() * 2, v.getHeight() * 2, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.scale(2, 2);
            g.setColor(T.BG);
            g.fillRect(0, 0, v.getWidth(), v.getHeight());
            v.paint(g);
            g.dispose();
            javax.imageio.ImageIO.write(img, "png", f);
            flash("Saved " + f.getName());
        } catch (Exception ex) {
            flash("Could not save the image");
        }
    }

    // Save the built-in examples and current custom/random case as text.
    void exportReport() {
        File f = choose("Deadlock_Report.txt");
        if (f == null) return;
        try {
            PrintWriter w = new PrintWriter(f, "UTF-8");
            w.print(Report.full());
            if ("Custom".equals(caseName) || "Random".equals(caseName))
                w.print(Report.caseReport(caseName + " system", alloc, req, avail));
            w.close();
            flash("Saved " + f.getName());
        } catch (Exception ex) {
            flash("Could not save the report");
        }
    }

    // Use --report for console output; otherwise launch the Swing interface.
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--report")) {   
            System.out.println(Report.full());
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception ignored) {
                }
                new DeadlockDetective().show();
            }
        });
    }
}
