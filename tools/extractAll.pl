#!/usr/bin/perl -w

use strict;
use List::Util qw[min max];

my $langfile = $ARGV[0];

die("syntax: extractAll.pl language_file") 
    unless defined $langfile;

open(LANG, $langfile);

my @lang = ();

while(<LANG>){
    chomp;
    my @curlang = split(/\s+/);
 
    push(@lang, @curlang);
}

my $len = $#lang + 1;
my $en = "en";
my $wikidir = "/mnt/data/wiki";
my $dumpsdir = "$wikidir/dumps";
my $outdir = "";

for (my $i = 0; $i < $len; $i++) {

    print "Starting extraction for $lang[$i].\n";

    $outdir = "$wikidir/pages/$lang[$i]";

    system("mkdir $outdir");
    system("nohup nice bash ./extract.sh $lang[$i] $en $dumpsdir $outdir &> $outdir/$lang[$i].out &");
}
