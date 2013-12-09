stats "results/tmp/three_hosts.wrappedplayground.tsv" using 1 nooutput
maxruns = STATS_blocks - 1

set terminal pngcairo size 800,600 enhanced font 'Verdana,10'

set xlabel "Simulated Latency (ns)"
set ylabel "Time (ms)"

set logscale xy

set xrange [1000:]

filename(s,n) = sprintf("results/tmp/%s.%s.tsv", s, n)

testlist = "three_hosts three_hosts_with_many_sources three_hosts_with_many_changing_sources" #signal_chain signal_fan 
wrapperlist = "wrappedplayground wrappedscalareact wrappedscalarx wrappedscalarxparallel hackkedelmsimulation"


do for [run = 0:maxruns] {
  do for [test in testlist] {
    set output sprintf("results/%s_run%02d.png", test, run)
    plot for [wrapper in wrapperlist]\
      filename(test, wrapper) every ::2 index(run)\
      using (column("param-nanosleep")):(column("value"))\
      title wrapper[8:] with linespoints
  }
}

# i dont know why this is needed, and i kind of dont care :D
set xrange [0:]

unset logscale x

set boxwidth 0.9 absolute
set style fill   solid 1.00 border lt -1
set key inside right top vertical Right noreverse noenhanced autotitles nobox
set style histogram clustered gap 1 title  offset character 0, 0, 0
set datafile missing '-'
set style data histograms
set xtics border in scale 0,0 nomirror rotate by -45  offset character 0, 0, 0 autojustify
set xtics  norangelimit font ",8"
set xtics ()


do for [run = 0:maxruns] {
  do for [wrapper in wrapperlist] {

    set output sprintf("results/%s_run%02d.png", wrapper, run)

    plot for [test in testlist]\
      filename(test, wrapper) every ::2 index (run)\
      using (column("value")):xtic(1) title test

  }
}
