set terminal pdf

set xlabel "Order Size"
set ylabel "Time (ms)"

set output "times.pdf"

set logscale xy

plot "tmp/rmi.Test-0.dsv" using (column("param-size")):(column("value")) title 'rmi' with linespoints, \
     "tmp/reactives.Test-1.dsv" using (column("param-size")):(column("value")) title 'reactives' with linespoints, \
     "tmp/sockets.Test-2.dsv" using (column("param-size")):(column("value")) title 'sockets' with linespoints, \
     "tmp/pure calculation.Test-3.dsv" using (column("param-size")):(column("value")) title 'calc' with linespoints

