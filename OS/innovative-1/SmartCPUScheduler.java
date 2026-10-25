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

public class SmartCPUScheduler {

    enum Algo {
        FCFS("FCFS", "Non-preemptive",
                "First-Come, First-Served. Processes run strictly in the order they arrive."),
        SJF("SJF", "Non-preemptive",
                "Shortest Job First. The ready process with the smallest burst runs next and is never interrupted."),
        SRTF("SRTF", "Preemptive",
                "Shortest Remaining Time First. A new arrival with less remaining work preempts the running process."),
        PRIORITY("Priority", "Non-preemptive",
                "Priority scheduling. The lowest number is the highest priority; a running process finishes first."),
        PRIORITY_P("Priority (P)", "Preemptive",
                "Preemptive priority. A higher-priority arrival takes the CPU immediately."),
        AGING("Priority + Aging", "Preemptive, anti-starvation",
                "Preemptive priority with aging. Each time a process waits for N units, its priority improves by one."),
        RR("Round Robin", "Preemptive, time quantum",
                "Round Robin. Every process gets a fixed time quantum, then rejoins the back of the queue.");

        final String title, tag, desc;

        Algo(String title, String tag, String desc) {
            this.title = title;
            this.tag = tag;
            this.desc = desc;
        }
    }

    static final class Proc {
        String id;
        int at, bt, pri, color;
        int rem, ct, rt, eff, streak;

        Proc(String id, int at, int bt, int pri) {
            this.id = id;
            this.at = at;
            this.bt = bt;
            this.pri = pri;
        }

        Proc copy() {
            Proc p = new Proc(id, at, bt, pri);
            p.color = color;
            return p;
        }
    }

    static final class Slice {
        final String pid;
        final int color, start;
        int end;

        Slice(String pid, int color, int start, int end) {
            this.pid = pid;
            this.color = color;
            this.start = start;
            this.end = end;
        }
    }

    static final class Row {
        String id;
        int color, at, bt, pri, ct, tat, wt, rt;
    }

    static final class Result {
        Algo algo;
        String label;
        List<Slice> slices = new ArrayList<Slice>();
        List<Row> rows = new ArrayList<Row>();
        double awt, atat, art, util, thr, pwt, score;
        int cs, maxWt, makespan;
    }

    static final class Engine {

        static int key(Algo a, Proc p) {
            switch (a) {
                case SJF:
                    return p.bt;
                case SRTF:
                    return p.rem;
                case PRIORITY:
                case PRIORITY_P:
                case AGING:
                    return p.eff;
                default:
                    return 0;
            }
        }

        static boolean better(Algo a, Proc x, Proc y) {
            int kx = key(a, x), ky = key(a, y);
            if (kx != ky)
                return kx < ky;
            return x.at < y.at;
        }

        static Result run(Algo algo, List<Proc> input, int quantum, int agingStep) {
            List<Proc> ps = new ArrayList<Proc>();
            for (Proc p : input) {
                Proc c = p.copy();
                c.rem = c.bt;
                c.rt = -1;
                c.eff = c.pri;
                c.streak = 0;
                ps.add(c);
            }
            int n = ps.size();
            List<Proc> ready = new ArrayList<Proc>();
            Result res = new Result();
            res.algo = algo;
            res.label = algo == Algo.RR ? "Round Robin (q=" + quantum + ")" : algo.title;

            boolean preemptive = algo == Algo.SRTF || algo == Algo.PRIORITY_P || algo == Algo.AGING;
            Proc cur = null, pending = null, lastRun = null;
            int t = 0, done = 0, used = 0, switches = 0, guard = 0;

            while (done < n && guard++ < 1000000) {
                for (Proc p : ps)
                    if (p.at == t)
                        ready.add(p);
                if (pending != null) {
                    ready.add(pending);
                    pending = null;
                }

                Proc cand = null;
                if (algo == Algo.RR) {
                    cand = ready.isEmpty() ? null : ready.get(0);
                } else {
                    for (Proc p : ready)
                        if (cand == null || better(algo, p, cand))
                            cand = p;
                }
                Proc next = cur;
                if (cur == null)
                    next = cand;
                else if (preemptive && cand != null && key(algo, cand) < key(algo, cur))
                    next = cand;

                if (next != cur) {
                    if (cur != null)
                        ready.add(cur);
                    cur = next;
                    if (cur != null)
                        ready.remove(cur);
                    used = 0;
                }

                if (cur == null) {
                    addSlice(res.slices, null, 0, t);
                } else {
                    if (cur.rt < 0)
                        cur.rt = t - cur.at;
                    if (lastRun != null && lastRun != cur)
                        switches++;
                    lastRun = cur;
                    cur.rem--;
                    cur.streak = 0;
                    used++;
                    addSlice(res.slices, cur.id, cur.color, t);
                }

                for (Proc p : ready) {
                    p.streak++;
                    if (algo == Algo.AGING && agingStep > 0 && p.streak % agingStep == 0 && p.eff > 0)
                        p.eff--;
                }
                t++;

                if (cur != null) {
                    if (cur.rem == 0) {
                        cur.ct = t;
                        done++;
                        cur = null;
                    } else if (algo == Algo.RR && used >= quantum) {
                        pending = cur;
                        cur = null;
                    }
                }
            }

            
            double sw = 0, st = 0, sr = 0, wNum = 0, wDen = 0;
            int busy = 0, maxWt = 0;
            for (Proc p : ps) {
                Row r = new Row();
                r.id = p.id;
                r.color = p.color;
                r.at = p.at;
                r.bt = p.bt;
                r.pri = p.pri;
                r.ct = p.ct;
                r.tat = p.ct - p.at;
                r.wt = r.tat - p.bt;
                r.rt = p.rt;
                res.rows.add(r);
                sw += r.wt;
                st += r.tat;
                sr += r.rt;
                busy += p.bt;
                maxWt = Math.max(maxWt, r.wt);
                double w = 1.0 / (p.pri + 1); 
                wNum += r.wt * w;
                wDen += w;
            }
            res.makespan = t;
            res.awt = n == 0 ? 0 : sw / n;
            res.atat = n == 0 ? 0 : st / n;
            res.art = n == 0 ? 0 : sr / n;
            res.util = t == 0 ? 0 : 100.0 * busy / t;
            res.thr = t == 0 ? 0 : (double) n / t;
            res.cs = switches;
            res.maxWt = maxWt;
            res.pwt = wDen == 0 ? 0 : wNum / wDen;
            return res;
        }

        



        static void addSlice(List<Slice> l, String pid, int color, int t) {
            if (!l.isEmpty()) {
                Slice last = l.get(l.size() - 1);
                boolean same = pid == null ? last.pid == null : pid.equals(last.pid);
                if (same && last.end == t) {
                    last.end = t + 1;
                    return;
                }
            }
            l.add(new Slice(pid, color, t, t + 1));
        }

        



        static List<Result> runAll(List<Proc> ps, int q, int age) {
            List<Result> all = new ArrayList<Result>();
            for (Algo a : Algo.values())
                all.add(run(a, ps, q, age));
            score(all, ps);
            return all;
        }

        









        static void score(List<Result> all, List<Proc> ps) {
            Set<Integer> distinct = new HashSet<Integer>();
            for (Proc p : ps)
                distinct.add(p.pri);
            double wp = distinct.size() >= 3 ? 1.0 : 0.0;
            double mA = 1e18, mT = 1e18, mR = 1e18, mM = 1e18, mP = 1e18;
            for (Result r : all) {
                mA = Math.min(mA, r.awt);
                mT = Math.min(mT, r.atat);
                mR = Math.min(mR, r.art);
                mM = Math.min(mM, r.maxWt);
                mP = Math.min(mP, r.pwt);
            }
            for (Result r : all) {
                r.score = (norm(r.awt, mA) + norm(r.atat, mT) + norm(r.art, mR) + norm(r.maxWt, mM)
                        + wp * norm(r.pwt, mP)) / (4 + wp);
            }
        }

        static double norm(double v, double min) {
            return (v + 1) / (min + 1);
        }

        static int recommended(List<Result> all) {
            int b = 0;
            for (int i = 1; i < all.size(); i++)
                if (all.get(i).score < all.get(b).score - 1e-9)
                    b = i;
            return b;
        }

        
        static String profile(List<Proc> ps) {
            int n = ps.size();
            if (n == 0)
                return "No processes";
            double mean = 0;
            int minP = Integer.MAX_VALUE, maxP = Integer.MIN_VALUE;
            Set<Integer> d = new HashSet<Integer>();
            for (Proc p : ps) {
                mean += p.bt;
                minP = Math.min(minP, p.pri);
                maxP = Math.max(maxP, p.pri);
                d.add(p.pri);
            }
            mean /= n;
            double var = 0;
            for (Proc p : ps)
                var += (p.bt - mean) * (p.bt - mean);
            double cv = Math.sqrt(var / n) / mean;
            String kind = mean <= 3.5 ? "Short jobs" : cv >= 0.45 ? "Mixed jobs" : "Uniform jobs";
            String pr = d.size() >= 3 ? ", priorities matter (" + minP + " to " + maxP + ")"
                    : ", priorities nearly equal";
            return String.format(Locale.US, "%s: %d processes, average burst %.1f, burst variation %.2f%s",
                    kind, n, mean, cv, pr);
        }

        static String reason(Algo a) {
            switch (a) {
                case FCFS:
                    return "Jobs are similar in length and arrive in a favourable order, so the simplest policy has no overhead and no real convoy penalty.";
                case SJF:
                    return "Running the shortest ready job first keeps waiting low without the context-switch cost of preemption.";
                case SRTF:
                    return "Preempting for the shortest remaining time keeps average waiting lowest when burst lengths vary and jobs arrive at different moments.";
                case PRIORITY:
                    return "Priorities differ meaningfully, and honouring them without preemption keeps overhead low.";
                case PRIORITY_P:
                    return "Important jobs get the CPU the moment they arrive, so the high-priority work waits the least.";
                case AGING:
                    return "It respects priority while aging stops low-priority jobs from starving, the best balance of importance and fairness here.";
                default:
                    return "Short time slices give every job an early first turn, which suits this workload when responsiveness matters most.";
            }
        }
    }

    
    
    

    static final class Report {

        static String f2(double v) {
            return String.format(Locale.US, "%.2f", v);
        }

        static String rep(char c, int n) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++)
                sb.append(c);
            return sb.toString();
        }

        
        static String ascii(List<Slice> sl) {
            StringBuilder out = new StringBuilder();
            int i = 0;
            while (i < sl.size()) {
                int end = Math.min(sl.size(), i + 9);
                StringBuilder top = new StringBuilder("|"), bot = new StringBuilder();
                bot.append(sl.get(i).start);
                for (int k = i; k < end; k++) {
                    Slice s = sl.get(k);
                    String nm = s.pid == null ? "idle" : s.pid;
                    int w = Math.max(6, nm.length() + 2);
                    int padL = (w - nm.length()) / 2;
                    top.append(rep(' ', padL)).append(nm).append(rep(' ', w - nm.length() - padL)).append('|');
                    int col = top.length() - 1;
                    while (bot.length() < col)
                        bot.append(' ');
                    bot.append(s.end);
                }
                out.append("    ").append(top).append("\n    ").append(bot).append("\n");
                i = end;
            }
            return out.toString();
        }

        static String workload(String name, List<Proc> ps, int q, int age) {
            StringBuilder sb = new StringBuilder();
            sb.append(rep('=', 78)).append("\n TEST CASE: ").append(name).append("\n").append(rep('=', 78))
                    .append("\n");
            sb.append(" Time quantum = ").append(q).append(", aging step = ").append(age).append("\n");
            sb.append(" Profile: ").append(Engine.profile(ps)).append("\n\n");
            sb.append(String.format("    %-8s %8s %8s %9s\n", "Process", "Arrival", "Burst", "Priority"));
            for (Proc p : ps)
                sb.append(String.format("    %-8s %8d %8d %9d\n", p.id, p.at, p.bt, p.pri));
            List<Result> all = Engine.runAll(ps, q, age);
            for (Result r : all) {
                sb.append("\n--- ").append(r.label).append(" (").append(r.algo.tag).append(") ---\n");
                sb.append("  Gantt chart:\n").append(ascii(r.slices));
                sb.append(String.format("\n    %-8s %5s %5s %5s | %5s %5s %5s %5s\n", "Process", "AT", "BT", "Pri",
                        "CT", "TAT", "WT", "RT"));
                for (Row w : r.rows)
                    sb.append(String.format("    %-8s %5d %5d %5d | %5d %5d %5d %5d\n", w.id, w.at, w.bt, w.pri, w.ct,
                            w.tat, w.wt, w.rt));
                sb.append("  Average waiting time    : ").append(f2(r.awt)).append("\n");
                sb.append("  Average turnaround time : ").append(f2(r.atat)).append("\n");
                sb.append("  Average response time   : ").append(f2(r.art)).append("\n");
                sb.append("  CPU utilization         : ").append(f2(r.util)).append("%\n");
                sb.append("  Context switches        : ").append(r.cs).append("\n");
                sb.append("  Throughput              : ").append(f2(r.thr)).append(" processes/unit\n");
            }
            sb.append("\n").append(rep('-', 78)).append("\n COMPARISON\n").append(rep('-', 78)).append("\n");
            sb.append(String.format(" %-18s %7s %7s %7s %7s %5s %7s %7s\n", "Algorithm", "AvgWT", "AvgTAT", "AvgRT",
                    "CPU%", "CS", "MaxWT", "Score"));
            int rec = Engine.recommended(all);
            for (int i = 0; i < all.size(); i++) {
                Result r = all.get(i);
                sb.append(String.format(" %-18s %7s %7s %7s %7s %5d %7d %7s%s\n", r.label, f2(r.awt), f2(r.atat),
                        f2(r.art),
                        f2(r.util), r.cs, r.maxWt, f2(r.score), i == rec ? "  <- recommended" : ""));
            }
            sb.append("\n Recommended algorithm: ").append(all.get(rec).label).append("\n ")
                    .append(Engine.reason(all.get(rec).algo)).append("\n\n Analysis:\n");
            for (String s : insights(all, q, age))
                sb.append("  * ").append(s).append("\n");
            sb.append("\n");
            return sb.toString();
        }

        



        static String full(List<Proc> current, String currentName, int q, int age) {
            StringBuilder sb = new StringBuilder();
            sb.append("SMART CPU SCHEDULER - TEST REPORT\n\n");
            for (int i = 0; i < PRESET_NAMES.length; i++)
                sb.append(workload(PRESET_NAMES[i], preset(i), q, age));
            if (current != null && !current.isEmpty() && "Custom".equals(currentName))
                sb.append(workload("Custom workload", current, q, age));
            return sb.toString();
        }

        static Result by(List<Result> all, Algo a) {
            return all.get(a.ordinal());
        }

        



        static List<String> insights(List<Result> all, int q, int age) {
            List<String> out = new ArrayList<String>();
            Result bw = all.get(0), ww = all.get(0), br = all.get(0);
            for (Result r : all) {
                if (r.awt < bw.awt)
                    bw = r;
                if (r.awt > ww.awt)
                    ww = r;
                if (r.art < br.art)
                    br = r;
            }
            double gap = ww.awt > 0 ? (ww.awt - bw.awt) / ww.awt * 100 : 0;
            out.add(String.format(Locale.US,
                    "Waiting time: %s is best at %.2f and %s is worst at %.2f, a %.0f%% difference, so the choice of policy clearly matters for this workload.",
                    bw.label, bw.awt, ww.label, ww.awt, gap));

            Result fcfs = by(all, Algo.FCFS), sjf = by(all, Algo.SJF), srtf = by(all, Algo.SRTF);
            Result rr = by(all, Algo.RR), pn = by(all, Algo.PRIORITY), pp = by(all, Algo.PRIORITY_P),
                    ag = by(all, Algo.AGING);

            out.add(String.format(Locale.US,
                    "Responsiveness: %s gives the fastest first response (%.2f on average). Round Robin with quantum %d responds in %.2f and %s.",
                    br.label, br.art, q, rr.art,
                    rr.cs > fcfs.cs ? "pays " + rr.cs + " context switches against " + fcfs.cs + " for FCFS"
                            : "needs no extra context switches here"));

            if (fcfs.awt > sjf.awt * 1.15)
                out.add(String.format(Locale.US,
                        "FCFS shows the convoy effect: a longer job that arrives early holds up shorter ones behind it, giving %.2f average waiting against %.2f for SJF.",
                        fcfs.awt, sjf.awt));
            else
                out.add(String.format(Locale.US,
                        "FCFS stays close to SJF here (%.2f against %.2f) because the arrival order already favours short jobs, so its simplicity costs little.",
                        fcfs.awt, sjf.awt));

            if (srtf.awt < sjf.awt - 0.005)
                out.add(String.format(Locale.US,
                        "Preemption pays off: SRTF lowers average waiting from %.2f (SJF) to %.2f by letting short arrivals jump the queue, using %d context switches instead of %d.",
                        sjf.awt, srtf.awt, srtf.cs, sjf.cs));
            else
                out.add(String.format(Locale.US,
                        "SRTF and SJF perform alike here (%.2f and %.2f), so preemption brings extra switches without a gain.",
                        srtf.awt, sjf.awt));

            out.add(String.format(Locale.US,
                    "Priority scheduling serves important jobs first but can starve the rest: the longest wait is %d units without preemption and %d with it. Aging (step %d) %s the preemptive worst case %s %d, %s.",
                    pn.maxWt, pp.maxWt, age, ag.maxWt < pp.maxWt ? "reduces" : "keeps",
                    ag.maxWt < pp.maxWt ? "to" : "at", ag.maxWt,
                    ag.awt > pp.awt ? "at a small cost in average waiting" : "without hurting average waiting"));
            return out;
        }
    }

    
    
    

    static final String[] PRESET_NAMES = { "Short jobs", "Mixed jobs", "High priority" };

    
    static final int[][][] PRESETS = {
            { { 0, 2, 3 }, { 1, 1, 2 }, { 2, 3, 4 }, { 3, 2, 1 }, { 4, 1, 5 }, { 5, 2, 2 } },
            { { 0, 12, 3 }, { 1, 3, 2 }, { 2, 8, 4 }, { 3, 2, 1 }, { 5, 6, 5 }, { 6, 4, 3 } },
            { { 0, 5, 6 }, { 1, 4, 1 }, { 2, 6, 2 }, { 3, 3, 1 }, { 4, 5, 3 }, { 6, 4, 1 }, { 7, 3, 5 } }
    };

    static List<Proc> preset(int i) {
        List<Proc> l = new ArrayList<Proc>();
        for (int k = 0; k < PRESETS[i].length; k++) {
            int[] a = PRESETS[i][k];
            Proc p = new Proc("P" + (k + 1), a[0], a[1], a[2]);
            p.color = k;
            l.add(p);
        }
        return l;
    }

    
    
    

    
    static final class T {
        static final Color BG = new Color(0xF2F4F3), CARD = Color.WHITE, LINE = new Color(0xE2E6E4),
                ROW = new Color(0xF0F2F1), INK = new Color(0x101C1A), MUTED = new Color(0x667774),
                FAINT = new Color(0xA3AEAB), ACCENT = new Color(0x0B7A6B), ACCENT_HOVER = new Color(0x096A5D),
                ACCENT_SOFT = new Color(0xE3F2EF), ACCENT_MID = new Color(0xB5DCD5), TRACK = new Color(0xE8ECEA);
        static final Color[] PAL = {
                new Color(0xF4A261), new Color(0x6CA6E8), new Color(0xE76F8B), new Color(0x9B8CE0),
                new Color(0x8FCB74), new Color(0xF2CF55), new Color(0x5CC1D6), new Color(0xE89A7B),
                new Color(0xB6A38E), new Color(0xC678B8) };
        static final String FAMILY = pick();

        static String pick() {
            Set<String> have = new HashSet<String>(Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            String[] want = { "Inter", "SF Pro Text", "Segoe UI", "Helvetica Neue", "Roboto", "Noto Sans",
                    "DejaVu Sans" };
            for (String w : want)
                if (have.contains(w))
                    return w;
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
            g.drawString(s, (float) (cx - fm.stringWidth(s) / 2.0),
                    (float) (cy + (fm.getAscent() - fm.getDescent()) / 2.0));
        }

        
        static void anim(JComponent c, int ms, final DoubleConsumer step) {
            Object old = c.getClientProperty("anim");
            if (old instanceof Timer)
                ((Timer) old).stop();
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
                if (!c.isVisible())
                    continue;
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
                if (!c.isVisible())
                    continue;
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
            RoundRectangle2D.Float s = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, arc,
                    arc);
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

    
    static class Btn extends JButton {
        enum Kind {
            PRIMARY, SOFT, GHOST
        }

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
            if (fw > 0)
                return new Dimension(fw, fh);
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

    
    static class Chip extends JToggleButton {
        final Algo algo;
        boolean hover;

        Chip(Algo a) {
            this.algo = a;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(a.desc);
            setPreferredSize(new Dimension(150, 46));
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

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = T.aa(g);
            boolean on = isSelected();
            RoundRectangle2D.Float s = new RoundRectangle2D.Float(0.75f, 0.75f, getWidth() - 1.5f, getHeight() - 1.5f,
                    12, 12);
            g2.setColor(on ? T.ACCENT_SOFT : hover ? new Color(0xF7F9F8) : T.CARD);
            g2.fill(s);
            g2.setColor(on ? T.ACCENT : T.LINE);
            g2.setStroke(new BasicStroke(on ? 1.6f : 1f));
            g2.draw(s);
            g2.setFont(T.f(Font.BOLD, 13));
            g2.setColor(on ? T.ACCENT : T.INK);
            g2.drawString(algo.title, 12, 20);
            g2.setFont(T.f(Font.PLAIN, 11));
            g2.setColor(T.MUTED);
            String tag = algo.tag.length() > 22 ? algo.tag.substring(0, algo.tag.indexOf(',')) : algo.tag;
            g2.drawString(tag, 12, 35);
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
                if (onChange != null)
                    onChange.run();
            }
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

    
    static class Logo extends JComponent {
        Logo() {
            setPreferredSize(new Dimension(42, 42));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = T.aa(g);
            g2.setPaint(new GradientPaint(0, 0, new Color(0x13A18C), 42, 42, new Color(0x08574D)));
            g2.fill(new RoundRectangle2D.Float(0, 0, 42, 42, 12, 12));
            g2.setColor(new Color(255, 255, 255, 235));
            g2.fill(new RoundRectangle2D.Float(9, 10, 14, 5, 5, 5));
            g2.fill(new RoundRectangle2D.Float(15, 18.5f, 18, 5, 5, 5));
            g2.fill(new RoundRectangle2D.Float(9, 27, 11, 5, 5, 5));
            g2.setColor(new Color(0xF4A261));
            g2.fill(new RoundRectangle2D.Float(25, 27, 8, 5, 5, 5));
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
            if (r.isEmpty())
                return;
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
            l.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, T.ROW),
                    new EmptyBorder(0, c == 0 ? 12 : 4, 0, c == 0 ? 12 : 4)));
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
            l.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, T.LINE),
                    new EmptyBorder(0, c == 0 ? 12 : 4, 0, c == 0 ? 12 : 4)));
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
                } else
                    cur = new StringBuilder(test);
            }
            if (cur.length() > 0)
                lines.add(cur.toString());
            return lines;
        }

        int measure(int width) {
            FontMetrics fm = getFontMetrics(getFont());
            int lh = fm.getHeight() + 3, h = 0;
            for (String p : paras)
                h += wrap(fm, p, Math.max(80, width - indent())).size() * lh + 9;
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

    

    



    static class Gantt extends JPanel implements Scrollable {
        List<Result> rows = new ArrayList<Result>();
        final boolean compact;
        double reveal = 0;
        int total = 0;

        Gantt(boolean compact) {
            this.compact = compact;
            setOpaque(false);
        }

        int leftPad() {
            return compact ? 140 : 8;
        }

        int barH() {
            return compact ? 26 : 58;
        }

        int gap() {
            return compact ? 12 : 0;
        }

        int top() {
            return compact ? 8 : 14;
        }

        double minUnit() {
            return compact ? 18 : 24;
        }

        void setRows(List<Result> r, boolean animate) {
            rows = r;
            total = 0;
            for (Result x : r)
                total = Math.max(total, x.makespan);
            reveal = animate ? 0 : total;
            revalidate();
            repaint();
            if (animate && total > 0) {
                final int tot = total;
                T.anim(this, Math.min(1800, Math.max(700, tot * 55)), new DoubleConsumer() {
                    public void accept(double p) {
                        reveal = tot * p;
                        repaint();
                    }
                });
            }
        }

        @Override
        public Dimension getPreferredSize() {
            int n = Math.max(1, rows.size());
            return new Dimension((int) (leftPad() + 12 + Math.max(total, 1) * minUnit()),
                    top() + n * barH() + (n - 1) * gap() + 50);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = T.aa(g);
            if (rows.isEmpty() || total == 0) {
                g2.setFont(T.f(Font.PLAIN, 13));
                g2.setColor(T.MUTED);
                T.center(g2, "Add a process to see the Gantt chart", getWidth() / 2.0, getHeight() / 2.0);
                g2.dispose();
                return;
            }
            int lp = leftPad();
            double unit = Math.max(minUnit(), (getWidth() - lp - 12.0) / total);
            int bh = barH(), arc = compact ? 8 : 12;
            int axisY = top() + rows.size() * bh + (rows.size() - 1) * gap() + 10;

            
            TreeSet<Integer> marks = new TreeSet<Integer>();
            if (compact) {
                int step = Math.max(1, (int) Math.ceil(30 / unit));
                if (unit < 11)
                    step = Math.max(step, 2);
                for (int k = 0; k <= total; k += step)
                    marks.add(k);
                marks.add(total);
            } else {
                marks.add(0);
                for (Slice s : rows.get(0).slices)
                    marks.add(s.end);
            }
            
            if (compact) {
                g2.setColor(new Color(0xEEF1F0));
                for (int m : marks) {
                    double x = lp + m * unit;
                    g2.draw(new Line2D.Double(x, top() - 4, x, axisY));
                }
            }

            for (int r = 0; r < rows.size(); r++) {
                Result res = rows.get(r);
                int y = top() + r * (bh + gap());
                if (compact) {
                    g2.setFont(T.f(Font.BOLD, 12));
                    g2.setColor(T.INK);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(res.label, 6, y + bh / 2f + (fm.getAscent() - fm.getDescent()) / 2f);
                }
                for (Slice s : res.slices) {
                    double ve = Math.min(s.end, reveal);
                    if (ve <= s.start)
                        continue;
                    double x = lp + s.start * unit, w = (ve - s.start) * unit;
                    RoundRectangle2D.Double rr = new RoundRectangle2D.Double(x + 1, y, Math.max(1, w - 2), bh, arc,
                            arc);
                    boolean full = ve >= s.end;
                    if (s.pid == null) {
                        g2.setColor(new Color(0xEDF0EF));
                        g2.fill(rr);
                        g2.setColor(new Color(0xC5CDCA));
                        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
                                new float[] { 4f, 3f }, 0f));
                        g2.draw(rr);
                        g2.setStroke(new BasicStroke(1f));
                        g2.setFont(T.f(Font.PLAIN, compact ? 10 : 12));
                        g2.setColor(T.MUTED);
                        if (full && w > 36)
                            T.center(g2, "idle", x + w / 2, y + bh / 2.0);
                    } else {
                        g2.setColor(T.PAL[s.color % T.PAL.length]);
                        g2.fill(rr);
                        g2.setFont(T.f(Font.BOLD, compact ? 11 : 13));
                        g2.setColor(new Color(0x101C1A));
                        FontMetrics fm = g2.getFontMetrics();
                        if (full && w - 2 > fm.stringWidth(s.pid) + 2)
                            T.center(g2, s.pid, x + w / 2, y + bh / 2.0);
                    }
                }
            }

            
            g2.setColor(new Color(0xC9D1CE));
            double axisEnd = lp + Math.min(reveal, total) * unit;
            g2.draw(new Line2D.Double(lp, axisY, axisEnd, axisY));
            g2.setFont(T.f(Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            double lastRight = -100;
            for (int m : marks) {
                if (m > reveal + 1e-9)
                    break;
                double x = lp + m * unit;
                String lbl = String.valueOf(m);
                double half = fm.stringWidth(lbl) / 2.0;
                g2.setColor(new Color(0xC9D1CE));
                g2.draw(new Line2D.Double(x, axisY, x, axisY + 4));
                if (x - half > lastRight + 4 || m == 0) {
                    g2.setColor(T.MUTED);
                    g2.drawString(lbl, (float) (x - half), axisY + 18);
                    lastRight = x + half;
                }
            }

            
            if (reveal < total - 1e-6) {
                g2.setColor(T.ACCENT);
                g2.setStroke(new BasicStroke(2f));
                double x = lp + reveal * unit;
                g2.draw(new Line2D.Double(x, top() - 4, x, axisY + 2));
            }
            g2.dispose();
        }

        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
            return 24;
        }

        public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
            return 200;
        }

        public boolean getScrollableTracksViewportWidth() {
            Container p = getParent();
            return p instanceof JViewport && p.getWidth() >= getPreferredSize().width;
        }

        public boolean getScrollableTracksViewportHeight() {
            return true;
        }
    }

    

    
    static class Bars extends Card {
        List<String> labels = new ArrayList<String>();
        double[][] vals = new double[3][0];
        final String[] heads = { "Average waiting time", "Average turnaround time", "Average response time" };
        double progress = 1;

        Bars() {
            super();
        }

        void set(List<Result> all, boolean animate) {
            labels.clear();
            vals = new double[3][all.size()];
            for (int i = 0; i < all.size(); i++) {
                Result r = all.get(i);
                labels.add(r.label);
                vals[0][i] = r.awt;
                vals[1][i] = r.atat;
                vals[2][i] = r.art;
            }
            progress = animate ? 0 : 1;
            revalidate();
            repaint();
            if (animate)
                T.anim(this, 900, new DoubleConsumer() {
                    public void accept(double p) {
                        progress = p;
                        repaint();
                    }
                });
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(300, 74 + labels.size() * 32 + 14);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = T.aa(g);
            int n = labels.size();
            if (n == 0) {
                g2.dispose();
                return;
            }
            int x0 = 20, labelW = 128;
            int colW = (getWidth() - x0 - 20 - labelW) / 3;
            g2.setFont(T.f(Font.BOLD, 14));
            g2.setColor(T.INK);
            g2.drawString("Average times (lower is better)", x0, 32);
            g2.setFont(T.f(Font.PLAIN, 12));
            g2.setColor(T.MUTED);
            for (int m = 0; m < 3; m++)
                g2.drawString(heads[m], x0 + labelW + m * colW, 58);
            for (int i = 0; i < n; i++) {
                int y = 74 + i * 32;
                g2.setFont(T.f(Font.PLAIN, 12));
                g2.setColor(T.INK);
                g2.drawString(labels.get(i), x0, y + 17);
                for (int m = 0; m < 3; m++) {
                    double max = 0, min = 1e18;
                    for (double v : vals[m]) {
                        max = Math.max(max, v);
                        min = Math.min(min, v);
                    }
                    if (max <= 0)
                        max = 1;
                    double v = vals[m][i];
                    boolean best = v <= min + 1e-9;
                    double barMax = colW - 58;
                    double bw = Math.max(3, v / max * barMax * progress);
                    double bx = x0 + labelW + m * colW;
                    g2.setColor(best ? T.ACCENT : T.ACCENT_MID);
                    g2.fill(new RoundRectangle2D.Double(bx, y + 6, bw, 14, 6, 6));
                    g2.setFont(T.f(best ? Font.BOLD : Font.PLAIN, 12));
                    g2.setColor(best ? T.ACCENT : T.MUTED);
                    g2.drawString(Report.f2(v * progress), (float) (bx + bw + 8), y + 18);
                }
            }
            g2.dispose();
        }
    }

    
    
    

    final JPanel root = new JPanel(new BorderLayout());
    final List<Proc> procs = new ArrayList<Proc>();
    Algo sel = Algo.SRTF;
    String workloadName = "Mixed jobs";
    List<Result> all = new ArrayList<Result>();
    boolean[][] bestMask = new boolean[0][0];
    int recIdx = -1;
    final Random rnd = new Random();

    
    ProcModel procModel;
    JTable procTable;
    JLabel workloadLabel;
    Chip[] chips = new Chip[Algo.values().length];
    Stepper quantum, aging;

    
    JLabel schedTitle, schedDesc, status;
    Pill schedTag;
    JLabel[] statVals = new JLabel[6];
    Gantt gantt = new Gantt(false);
    JTable metricsTable;

    
    JLabel recName, recProfile;
    Notes recReason, insightNotes;
    Bars bars = new Bars();
    JTable compTable;
    Gantt ganttAll = new Gantt(true);
    Btn recView;

    JPanel pages;
    VPanel schedPage, compPage;
    Component currentPage;
    Seg segSched, segComp;

    SmartCPUScheduler() {
        root.setBackground(T.BG);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMain(), BorderLayout.CENTER);
        loadPreset(1);
        chips[sel.ordinal()].setSelected(true);
        updateParams();
        refresh(true);
    }

    void show() {
        JFrame f = new JFrame("Smart CPU Scheduler");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setContentPane(root);
        Dimension sc = Toolkit.getDefaultToolkit().getScreenSize();
        f.setSize(Math.min(1400, sc.width - 60), Math.min(900, sc.height - 80));
        f.setMinimumSize(new Dimension(1100, 680));
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    

    class ProcModel extends AbstractTableModel {
        final String[] cols = { "Process", "Arrival", "Burst", "Priority", "" };

        public int getRowCount() {
            return procs.size();
        }

        public int getColumnCount() {
            return 5;
        }

        public String getColumnName(int c) {
            return cols[c];
        }

        public Object getValueAt(int r, int c) {
            Proc p = procs.get(r);
            switch (c) {
                case 0:
                    return new Tag(p.id, p.color);
                case 1:
                    return p.at;
                case 2:
                    return p.bt;
                case 3:
                    return p.pri;
                default:
                    return "\u00D7";
            }
        }

        public boolean isCellEditable(int r, int c) {
            return c >= 1 && c <= 3;
        }

        public void setValueAt(Object v, int r, int c) {
            int x;
            try {
                x = Integer.parseInt(String.valueOf(v).trim());
            } catch (Exception e) {
                return;
            }
            Proc p = procs.get(r);
            if (c == 1)
                p.at = Math.max(0, Math.min(99, x));
            else if (c == 2)
                p.bt = Math.max(1, Math.min(99, x));
            else if (c == 3)
                p.pri = Math.max(0, Math.min(99, x));
            fireTableRowsUpdated(r, r);
            onEdited();
        }
    }

    

    JComponent buildSidebar() {
        VPanel side = new VPanel(14);
        side.setBorder(new EmptyBorder(24, 22, 22, 12));

        
        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.add(new Logo(), BorderLayout.WEST);
        VPanel bt = new VPanel(1);
        bt.add(lab("Smart CPU Scheduler", Font.BOLD, 18, T.INK));
        bt.add(lab("Simulate, visualize and compare", Font.PLAIN, 12, T.MUTED));
        brand.add(bt, BorderLayout.CENTER);
        side.add(brand);

        
        Card wc = new Card();
        wc.setLayout(new VLayout(10));
        wc.setBorder(new EmptyBorder(16, 16, 16, 16));
        JPanel wh = new JPanel(new BorderLayout());
        wh.setOpaque(false);
        wh.add(lab("Processes", Font.BOLD, 14, T.INK), BorderLayout.WEST);
        workloadLabel = lab("", Font.PLAIN, 12, T.MUTED);
        wh.add(workloadLabel, BorderLayout.EAST);
        wc.add(wh);

        JPanel presets = new JPanel(new GridLayout(1, 3, 6, 0));
        presets.setOpaque(false);
        for (int i = 0; i < PRESET_NAMES.length; i++) {
            final int idx = i;
            Btn b = new Btn(PRESET_NAMES[i], Btn.Kind.SOFT).compact();
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    loadPreset(idx);
                    refresh(true);
                }
            });
            presets.add(b);
        }
        wc.add(presets);

        procModel = new ProcModel();
        procTable = new JTable(procModel);
        styleTable(procTable, 34);
        procTable.setRowSelectionAllowed(true);
        procTable.setSelectionBackground(T.ACCENT_SOFT);
        procTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
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
        procTable.setDefaultEditor(Object.class, ed);
        TableColumnModel cm = procTable.getColumnModel();
        cm.getColumn(0).setPreferredWidth(84);
        cm.getColumn(1).setPreferredWidth(66);
        cm.getColumn(2).setPreferredWidth(66);
        cm.getColumn(3).setPreferredWidth(70);
        cm.getColumn(4).setPreferredWidth(34);
        cm.getColumn(4).setCellRenderer(new CellR() {
            @Override
            void post(JLabel l, JTable t, Object v, int r, int c) {
                l.setForeground(T.FAINT);
                l.setFont(T.f(Font.PLAIN, 16));
                l.setHorizontalAlignment(SwingConstants.CENTER);
            }
        });
        procTable.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                int r = procTable.rowAtPoint(e.getPoint()), c = procTable.columnAtPoint(e.getPoint());
                if (r >= 0 && c == 4) {
                    if (procTable.isEditing())
                        procTable.getCellEditor().cancelCellEditing();
                    procs.remove(r);
                    procModel.fireTableDataChanged();
                    onEdited();
                }
            }
        });
        JScrollPane tsp = scroll(procTable, T.CARD);
        tsp.setPreferredSize(new Dimension(10, 34 + 34 * 5 + 2));
        wc.add(tsp);

        JPanel acts = new JPanel(new GridLayout(1, 3, 6, 0));
        acts.setOpaque(false);
        Btn add = new Btn("Add process", Btn.Kind.SOFT).compact();
        Btn rand = new Btn("Random", Btn.Kind.SOFT).compact();
        Btn clear = new Btn("Clear", Btn.Kind.GHOST).compact();
        add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addProc();
            }
        });
        rand.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                randomWorkload();
            }
        });
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (procTable.isEditing())
                    procTable.getCellEditor().cancelCellEditing();
                procs.clear();
                procModel.fireTableDataChanged();
                onEdited();
            }
        });
        acts.add(add);
        acts.add(rand);
        acts.add(clear);
        wc.add(acts);
        side.add(wc);

        
        Card ac = new Card();
        ac.setLayout(new VLayout(10));
        ac.setBorder(new EmptyBorder(16, 16, 16, 16));
        ac.add(lab("Algorithm", Font.BOLD, 14, T.INK));
        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 8));
        grid.setOpaque(false);
        ButtonGroup bg = new ButtonGroup();
        for (final Algo a : Algo.values()) {
            Chip c = new Chip(a);
            chips[a.ordinal()] = c;
            bg.add(c);
            grid.add(c);
            c.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    sel = a;
                    updateParams();
                    refresh(true);
                    showPage(false);
                }
            });
        }
        ac.add(grid);

        quantum = new Stepper(2, 1, 20);
        aging = new Stepper(3, 1, 20);
        Runnable again = new Runnable() {
            public void run() {
                refresh(false);
            }
        };
        quantum.onChange = again;
        aging.onChange = again;
        JPanel params = new JPanel(new GridLayout(1, 2, 12, 0));
        params.setOpaque(false);
        params.add(param("Quantum", quantum));
        params.add(param("Aging step", aging));
        ac.add(params);
        side.add(ac);

        Btn run = new Btn("Run simulation", Btn.Kind.PRIMARY);
        run.setFont(T.f(Font.BOLD, 14));
        run.h = 46;
        run.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refresh(true);
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

    void updateParams() {
        quantum.setActive(sel == Algo.RR);
        aging.setActive(sel == Algo.AGING);
    }

    

    JComponent buildMain() {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(24, 6, 0, 12));

        
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        Card segWrap = new Card(14, new Color(0xE4E9E7), null);
        segWrap.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
        segSched = new Seg("Schedule");
        segComp = new Seg("Compare all");
        ButtonGroup g = new ButtonGroup();
        g.add(segSched);
        g.add(segComp);
        segSched.setSelected(true);
        segWrap.add(segSched);
        segWrap.add(segComp);
        segSched.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPage(false);
            }
        });
        segComp.addActionListener(new ActionListener() {
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
        schedPage = buildSchedulePage();
        compPage = buildComparePage();
        pages.add(scroll(schedPage, T.BG), "sched");
        pages.add(scroll(compPage, T.BG), "comp");
        currentPage = schedPage;
        main.add(pages, BorderLayout.CENTER);
        return main;
    }

    void showPage(boolean compare) {
        ((CardLayout) pages.getLayout()).show(pages, compare ? "comp" : "sched");
        currentPage = compare ? compPage : schedPage;
        (compare ? segComp : segSched).setSelected(true);
    }

    VPanel buildSchedulePage() {
        VPanel p = new VPanel(16);
        p.setBorder(new EmptyBorder(0, 0, 24, 12));

        
        JPanel head = new JPanel(new VLayout(4));
        head.setOpaque(false);
        JPanel tr = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        tr.setOpaque(false);
        schedTitle = lab("", Font.BOLD, 26, T.INK);
        schedTag = new Pill();
        tr.add(schedTitle);
        tr.add(schedTag);
        head.add(tr);
        schedDesc = lab("", Font.PLAIN, 13, T.MUTED);
        JPanel dl = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        dl.setOpaque(false);
        dl.add(schedDesc);
        head.add(dl);
        p.add(head);

        
        Card strip = new Card();
        strip.setLayout(new GridLayout(1, 6));
        String[] names = { "Average waiting", "Average turnaround", "Average response", "CPU utilization",
                "Context switches", "Throughput" };
        for (int i = 0; i < 6; i++) {
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

        
        Card gc = new Card();
        gc.setLayout(new VLayout(6));
        gc.setBorder(new EmptyBorder(16, 18, 12, 18));
        gc.add(lab("Gantt chart", Font.BOLD, 14, T.INK));
        JScrollPane gs = scroll(gantt, T.CARD);
        gs.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        gs.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        gc.add(gs);
        p.add(gc);

        
        Card mc = new Card();
        mc.setLayout(new VLayout(0));
        mc.setBorder(new EmptyBorder(16, 6, 8, 6));
        JPanel mh = new JPanel(new BorderLayout());
        mh.setOpaque(false);
        mh.setBorder(new EmptyBorder(0, 12, 10, 12));
        mh.add(lab("Process metrics", Font.BOLD, 14, T.INK), BorderLayout.WEST);
        mc.add(mh);
        metricsTable = new JTable(new SimpleModel(new String[0], new Object[0][0]));
        styleTable(metricsTable, 36);
        metricsTable.setRowSelectionAllowed(false);
        metricsTable.setFocusable(false);
        mc.add(metricsTable.getTableHeader());
        mc.add(metricsTable);
        p.add(mc);
        return p;
    }

    VPanel buildComparePage() {
        VPanel p = new VPanel(16);
        p.setBorder(new EmptyBorder(0, 0, 24, 12));

        
        Card rc = new Card(14, T.ACCENT_SOFT, T.ACCENT_MID);
        rc.setLayout(new VLayout(6));
        rc.setBorder(new EmptyBorder(18, 22, 18, 22));
        rc.add(lab("Recommended for this workload", Font.BOLD, 13, T.ACCENT));
        recName = lab("", Font.BOLD, 28, T.INK);
        rc.add(recName);
        recReason = new Notes(false, 14, T.INK);
        rc.add(recReason);
        recProfile = lab("", Font.PLAIN, 12, T.MUTED);
        rc.add(recProfile);
        JPanel bh = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        bh.setOpaque(false);
        recView = new Btn("Open its Gantt chart", Btn.Kind.PRIMARY).compact();
        recView.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (recIdx >= 0)
                    selectAlgo(Algo.values()[recIdx]);
            }
        });
        bh.add(recView);
        rc.add(bh);
        p.add(rc);

        p.add(bars);

        
        Card tc = new Card();
        tc.setLayout(new VLayout(0));
        tc.setBorder(new EmptyBorder(16, 6, 8, 6));
        JPanel th = new JPanel(new BorderLayout());
        th.setOpaque(false);
        th.setBorder(new EmptyBorder(0, 12, 10, 12));
        th.add(lab("Performance measures", Font.BOLD, 14, T.INK), BorderLayout.WEST);
        th.add(lab("Click a row to open its schedule", Font.PLAIN, 12, T.MUTED), BorderLayout.EAST);
        tc.add(th);
        compTable = new JTable(new SimpleModel(new String[0], new Object[0][0]));
        styleTable(compTable, 36);
        compTable.setRowSelectionAllowed(false);
        compTable.setFocusable(false);
        compTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        compTable.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                int r = compTable.rowAtPoint(e.getPoint());
                if (r >= 0 && r < Algo.values().length)
                    selectAlgo(Algo.values()[r]);
            }
        });
        tc.add(compTable.getTableHeader());
        tc.add(compTable);
        p.add(tc);

        
        Card gc = new Card();
        gc.setLayout(new VLayout(6));
        gc.setBorder(new EmptyBorder(16, 18, 12, 18));
        gc.add(lab("Gantt charts, all algorithms", Font.BOLD, 14, T.INK));
        JScrollPane gs = scroll(ganttAll, T.CARD);
        gs.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        gs.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        gc.add(gs);
        p.add(gc);

        
        Card ic = new Card();
        ic.setLayout(new VLayout(10));
        ic.setBorder(new EmptyBorder(16, 22, 18, 22));
        ic.add(lab("How the algorithms behave on this workload", Font.BOLD, 14, T.INK));
        insightNotes = new Notes(true, 13, T.INK);
        ic.add(insightNotes);
        p.add(ic);
        return p;
    }

    

    void selectAlgo(Algo a) {
        sel = a;
        chips[a.ordinal()].setSelected(true);
        updateParams();
        refresh(true);
        showPage(false);
    }

    void loadPreset(int i) {
        if (procTable != null && procTable.isEditing())
            procTable.getCellEditor().cancelCellEditing();
        procs.clear();
        procs.addAll(preset(i));
        workloadName = PRESET_NAMES[i];
        procModel.fireTableDataChanged();
        workloadLabel.setText(workloadName);
    }

    void addProc() {
        if (procs.size() >= 20)
            return;
        if (procTable.isEditing())
            procTable.getCellEditor().stopCellEditing();
        int n = 0;
        for (Proc p : procs) {
            try {
                n = Math.max(n, Integer.parseInt(p.id.substring(1)));
            } catch (Exception ignored) {
            }
        }
        int lastAt = procs.isEmpty() ? -1 : procs.get(procs.size() - 1).at;
        Proc p = new Proc("P" + (n + 1), lastAt + 1, 4, 3);
        p.color = procs.size();
        procs.add(p);
        procModel.fireTableDataChanged();
        procTable.scrollRectToVisible(procTable.getCellRect(procs.size() - 1, 0, true));
        onEdited();
    }

    void randomWorkload() {
        if (procTable.isEditing())
            procTable.getCellEditor().cancelCellEditing();
        procs.clear();
        int n = 5 + rnd.nextInt(4), at = 0;
        for (int i = 0; i < n; i++) {
            if (i > 0)
                at += rnd.nextInt(4);
            int bt = rnd.nextInt(10) < 2 ? 8 + rnd.nextInt(8) : 1 + rnd.nextInt(6);
            Proc p = new Proc("P" + (i + 1), at, bt, 1 + rnd.nextInt(6));
            p.color = i;
            procs.add(p);
        }
        procModel.fireTableDataChanged();
        workloadName = "Random";
        workloadLabel.setText(workloadName);
        refresh(true);
    }

    
    void onEdited() {
        workloadName = "Custom";
        workloadLabel.setText(workloadName);
        refresh(false);
    }

    

    static String f2(double v) {
        return Report.f2(v);
    }

    void refresh(boolean animate) {
        schedTitle.setText(sel.title);
        schedTag.set(sel.tag);
        schedDesc.setText(sel.desc);

        if (procs.isEmpty()) {
            all = new ArrayList<Result>();
            gantt.setRows(all, false);
            ganttAll.setRows(all, false);
            for (JLabel l : statVals)
                l.setText("-");
            metricsTable.setModel(new SimpleModel(new String[0], new Object[0][0]));
            compTable.setModel(new SimpleModel(new String[0], new Object[0][0]));
            bars.set(all, false);
            recName.setText("Add processes to begin");
            recReason.set(new ArrayList<String>());
            recProfile.setText("");
            insightNotes.set(new ArrayList<String>());
            recIdx = -1;
            return;
        }

        all = Engine.runAll(procs, quantum.value, aging.value);
        Result cur = all.get(sel.ordinal());
        recIdx = Engine.recommended(all);

        
        statVals[0].setText(f2(cur.awt));
        statVals[1].setText(f2(cur.atat));
        statVals[2].setText(f2(cur.art));
        statVals[3].setText(String.format(Locale.US, "%.1f%%", cur.util));
        statVals[4].setText(String.valueOf(cur.cs));
        statVals[5].setText(f2(cur.thr) + "/unit");

        
        gantt.setRows(Collections.singletonList(cur), animate);

        
        Object[][] d = new Object[cur.rows.size() + 1][8];
        for (int i = 0; i < cur.rows.size(); i++) {
            Row r = cur.rows.get(i);
            d[i] = new Object[] { new Tag(r.id, r.color), r.at, r.bt, r.pri, r.ct, r.tat, r.wt, r.rt };
        }
        d[cur.rows.size()] = new Object[] { "Average", "", "", "", "", f2(cur.atat), f2(cur.awt), f2(cur.art) };
        final int avgRow = cur.rows.size();
        metricsTable.setModel(new SimpleModel(new String[] { "Process", "Arrival", "Burst", "Priority", "Completion",
                "Turnaround", "Waiting", "Response" }, d));
        metricsTable.setDefaultRenderer(Object.class, new CellR() {
            @Override
            void post(JLabel l, JTable t, Object v, int r, int c) {
                if (r == avgRow) {
                    l.setBackground(T.ACCENT_SOFT);
                    l.setForeground(T.ACCENT);
                    l.setFont(T.f(Font.BOLD, 13));
                }
                if (c >= 5 && r < avgRow)
                    l.setForeground(T.INK);
                if (c >= 1 && c <= 3 && r < avgRow)
                    l.setForeground(T.MUTED);
            }
        });

        
        Result rec = all.get(recIdx);
        recName.setText(rec.label);
        recReason.set(Collections.singletonList(Engine.reason(rec.algo)));
        recProfile.setText(Engine.profile(procs));
        bars.set(all, animate);
        ganttAll.setRows(all, animate);
        insightNotes.set(Report.insights(all, quantum.value, aging.value));
        fillCompareTable();
    }

    void fillCompareTable() {
        int n = all.size();
        Object[][] d = new Object[n][8];
        double[][] v = new double[n][7];
        for (int i = 0; i < n; i++) {
            Result r = all.get(i);
            v[i] = new double[] { 0, r.awt, r.atat, r.art, r.util, r.cs, r.maxWt, r.score };
            d[i] = new Object[] { (i == recIdx ? "\u2605  " : "") + r.label, f2(r.awt), f2(r.atat), f2(r.art),
                    String.format(Locale.US, "%.1f%%", r.util), r.cs, r.maxWt, f2(r.score) };
        }
        
        
        bestMask = new boolean[n][8];
        for (int c = 1; c < 8; c++) {
            double b = c == 4 ? -1e18 : 1e18;
            for (int i = 0; i < n; i++)
                b = c == 4 ? Math.max(b, v[i][c]) : Math.min(b, v[i][c]);
            for (int i = 0; i < n; i++)
                bestMask[i][c] = Math.abs(v[i][c] - b) < 1e-6;
        }
        compTable.setModel(new SimpleModel(new String[] { "Algorithm", "Avg waiting", "Avg turnaround", "Avg response",
                "CPU use", "Ctx switches", "Longest wait", "Fit score" }, d));
        compTable.getColumnModel().getColumn(0).setPreferredWidth(190);
        for (int c = 1; c < 8; c++)
            compTable.getColumnModel().getColumn(c).setPreferredWidth(105);
        compTable.setDefaultRenderer(Object.class, new CellR() {
            @Override
            void post(JLabel l, JTable t, Object val, int r, int c) {
                if (c == 0) {
                    l.setFont(T.f(Font.BOLD, 13));
                    if (r == recIdx)
                        l.setForeground(T.ACCENT);
                }
                if (r < bestMask.length && c > 0 && bestMask[r][c]) {
                    l.setForeground(T.ACCENT);
                    l.setFont(T.f(Font.BOLD, 13));
                } else if (c > 0)
                    l.setForeground(T.MUTED);
                if (r == recIdx)
                    l.setBackground(new Color(0xF3FAF8));
            }
        });
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
        if (fc.showSaveDialog(root) != JFileChooser.APPROVE_OPTION)
            return null;
        return fc.getSelectedFile();
    }

    



    void saveSnapshot() {
        File f = choose(
                segComp.isSelected() ? "comparison.png" : "schedule_" + sel.title.replaceAll("[^A-Za-z]", "") + ".png");
        if (f == null)
            return;
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

    



    void exportReport() {
        File f = choose("SmartCPU_Report.txt");
        if (f == null)
            return;
        try {
            PrintWriter w = new PrintWriter(f, "UTF-8");
            w.print(Report.full(procs, workloadName, quantum.value, aging.value));
            w.close();
            flash("Saved " + f.getName());
        } catch (Exception ex) {
            flash("Could not save the report");
        }
    }

    
    
    

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--report")) { 
            System.out.println(Report.full(null, "", 2, 3));
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception ignored) {
                }
                new SmartCPUScheduler().show();
            }
        });
    }
}
