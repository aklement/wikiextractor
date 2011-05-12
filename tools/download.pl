#!/usr/bin/perl -w

use strict;
use List::Util qw[min max];

my $langfile = $ARGV[0];
my $dumps_dir = $ARGV[1];

die("syntax: download.pl language_file dumps_dir") 
    unless defined $langfile && defined $dumps_dir;

open(LANG, $langfile);

my @lang = ();

while(<LANG>){
    chomp;
    my @curlang = split(/\s+/);
 
    push(@lang, @curlang);
}

my $len = $#lang + 1;
system("mkdir $dumps_dir");

for (my $i = 0; $i < $len; $i++) {
	print "Downloading $lang[$i] wiki to $dumps_dir ...\n";
	system("nohup nice wget http://dumps.wikimedia.org/$lang[$i]wiki/latest/$lang[$i]wiki-latest-pages-articles.xml.bz2 -o $dumps_dir/$lang[$i].log -P $dumps_dir &");
}
