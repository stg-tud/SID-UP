set terminal png

set xlabel "Iterations"
set ylabel "Time (ms)"

set logscale xy

filename(s) = sprintf("tmp/%s.tsv", s)

do for [run = 1:10] {
  do for [i = 0:5] {
    set output sprintf("tmp/run%d_size%d.png", run, 10 * (2**i))
    plot for [file in "rmi reactives sockets pure_calculation"] filename(file) index ((run - 1) * 6 + i) using (column("param-iterations")):(column("value")) title file with linespoints
  }
}
