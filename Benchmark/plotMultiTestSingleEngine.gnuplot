set terminal pngcairo size 800,600 enhanced font 'Verdana,10'

set xlabel "Simulated Latency (ns)"
set ylabel "Time (ms)"

#set logscale xy
set logscale y

set boxwidth 0.9 absolute
set style fill   solid 1.00 border lt -1
set key inside right top vertical Right noreverse noenhanced autotitles nobox
set style histogram clustered gap 1 title  offset character 0, 0, 0
set datafile missing '-'
set style data histograms
set xtics border in scale 0,0 nomirror rotate by -45  offset character 0, 0, 0 autojustify
set xtics  norangelimit font ",8"
set xtics ()

filename(s,n) = sprintf("results/tmp/%s.%s.tsv", s, n)

do for [run = 1:9] {
  do for [file in "wrappedplayground wrappedscalareact"] {

    set output sprintf("results/%s_run%02d.png", file, run)

    plot for [test in "signal_chain signal_fan three_hosts three_hosts_with_many_sources"] filename(test, file) index (run)\
        using (column("value")):xtic(1) title test

  }
}

