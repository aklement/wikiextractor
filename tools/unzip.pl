#!/usr/bin/perl -w

use strict;
use List::Util qw[min max];

my $langfile = $ARGV[0];
my $dumps_dir = $ARGV[1];

die("syntax: unzip.pl language_file") 
    unless defined $langfile && defined $dumps_dir;

open(LANG, $langfile);

my @lang = ();

while(<LANG>){
    chomp;
    my @curlang = split(/\s+/);
 
    push(@lang, @curlang);
}

my $len = $#lang + 1;

print "Unzipping languages from $lang[$from] to $lang[$to-1]!\n";
my $name;

for (my $i = 0; $i < $len; $i++) {
	$name = "$dumps_dir/$lang[$i]wiki-latest-pages-articles.xml.bz2";
    
	if (-e $name) {
		print "Unzipping $lang[$i] wiki ...\n";
		system("nohup nice bunzip2 $name &");
	} else {
		print "File $name does not exist!\n";
	}
}
