stats "results/tmp/three_hosts.wrappedplayground.tsv" using 1 nooutput
maxruns = STATS_blocks - 1

set format y "10^{%L}"

set terminal pdf size 4,3 enhanced font "Times New Roman, 14"

set xlabel "Simulated Latency (ms)" offset 0,.5
set ylabel "Time (ms)"

set logscale xy

set key right bottom

#set xrange [1000:]

prettyName(name) = \
  name eq "wrappedplayground" ? "SID-UP" : \
  name eq "wrappedscalareact" ? "scala.react" : \
  name eq "wrappedscalarx" ? "scala.rx sequential" : \
  name eq "wrappedscalarxparallel" ? "scala.rx parallel" : \
  name eq "hackkedelmsimulation" ? "ELM" : name

prettyTestName(name) = \
  name eq "three_hosts" ? "normal" : \
  name eq "three_hosts_with_many_sources" ? "random sources" : \
  name eq "three_hosts_with_many_changing_sources" ? "multi change" : \
  name eq "three_hosts_with_independent_sources" ? "independent sources" : name


filename(s,n) = sprintf("results/tmp/%s.%s.tsv", s, n)

#testlist = "three_hosts three_hosts_with_many_sources three_hosts_with_many_changing_sources three_hosts_with_independent_sources" #signal_chain signal_fan
testlist = "three_hosts three_hosts_with_many_sources"
wrapperlist = "wrappedplayground wrappedscalareact wrappedscalarx wrappedscalarxparallel hackkedelmsimulation"


do for [run = 0:maxruns] {
  do for [test in testlist] {
    set output sprintf("results/%s_run%02d.pdf", test, run)
    plot for [wrapper in wrapperlist]\
      filename(test, wrapper) every ::0 index(run)\
      using (column("param-nanosleep")/1000000):(column("value"))\
      title prettyName(wrapper) with linespoints pointsize 0.5 linewidth 3
  }
}

# i dont know why this is needed, and i kind of dont care :D
# set xrange [0:]

set xlabel "Simulated Latency (ns)"

unset logscale x

set boxwidth 0.9 absolute
set style fill   solid 1.00 border lt -1
set key inside left top vertical Right noreverse noenhanced autotitles nobox
set style histogram clustered gap 1 title  offset character 0, 0, 0
set datafile missing '-'
set style data histograms
set xtics border in scale 0,0 nomirror rotate by -45  offset character 0, 0, 0 autojustify
set xtics  norangelimit font ",8"
set xtics ()


do for [run = 0:maxruns] {
  do for [wrapper in wrapperlist] {

    set output sprintf("results/%s_run%02d.pdf", wrapper, run)

    plot for [test in testlist]\
      filename(test, wrapper) every ::0 index (run)\
      using (column("value")):xtic(1) title prettyTestName(test)

  }
}
