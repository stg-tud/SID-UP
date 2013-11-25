set terminal png

set xlabel "Simulated Latency (ns)"
set ylabel "Time (ms)"

set logscale xy

filename(s,n) = sprintf("results/tmp/%s.%s.tsv", s, n)

do for [run = 1:9] {
  do for [test in "signal_chain signal_fan three_hosts three_hosts_with_many_sources"] {
    set output sprintf("results/%s_run%02d.png", test, run)
    plot for [file in "wrappedplayground wrappedscalareact"] filename(test, file) index (run) using (column("param-nanosleep")):(column("value")) title file with linespoints
  }
}

