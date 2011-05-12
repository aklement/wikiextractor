#!/usr/bin/perl -w

use strict;
use List::Util qw[min max];

my $langfile = $ARGV[0];
my $dumps_dir = $ARGV[1];
my $out_dir = $ARGV[2];

die("syntax: extractAll.pl language_file dumps_dir out_dir") 
    unless defined $langfile && defined $dumps_dir && defined $out_dir;

open(LANG, $langfile);

my @lang = ();

while(<LANG>){
    chomp;
    my @curlang = split(/\s+/);
 
    push(@lang, @curlang);
}

my $len = $#lang + 1;
my $en = "af";
my $curdir = "";

for (my $i = 0; $i < $len; $i++) {

    print "Starting extraction for $lang[$i].\n";

    $curdir = "$out_dir/pages/$lang[$i]";

    system("mkdir -p $curdir");
    system("nohup nice bash ./extract.sh $lang[$i] $en $dumps_dir $curdir &> $curdir/$lang[$i].out &");
}
