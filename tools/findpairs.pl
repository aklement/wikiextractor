#!/usr/local/bin/perl
use File::Copy;

if ($#ARGV != 4) {
 print "usage: findpairs.pl lang_one lang_two page_path letter out_path\n";
 exit;
}

$langone = $ARGV[0];
$langtwo = $ARGV[1];
$letter = $ARGV[3];
$onepath = "$ARGV[2]/$langone/$letter";
$twopath = "$ARGV[2]/$langtwo/$letter";
$outpath = $ARGV[4];

print "Looking in $onepath and $twopath.\n";

my %pageset = ();

opendir(DIR, $onepath);
@FILES = readdir(DIR); 

foreach $file (@FILES) {

    if (($file =~ m/\.$langone$/) && ($file !~ m/\.wiki\.$langone$/)) {
        $file =~ s/\.$langone$//;
        $pageset{$file}++;
    }
}
closedir(DIR);

opendir(DIR, $twopath);
@FILES = readdir(DIR);

foreach $file (@FILES) {

    if (($file =~ m/\.$langtwo$/) && ($file !~ m/\.wiki\.$langtwo$/)) {
        $file =~ s/\.$langtwo$//;
        $pageset{$file}++;
    }
}
closedir(DIR);

print "Copying pairs to $outpath.\n";
my $numpairs = 0;

open (list, ">$outpath/list.txt");
my $dot = 0;

for my $page ( keys %pageset ) {
    my $num = $pageset{$page};

    if ($num == 2) {
	$numpairs++;
        
        print list "$page\n";
#        print "Copying $page\n";
        copy("$onepath/$page.$langone", "$outpath/$page.$langone");
        copy("$twopath/$page.$langtwo", "$outpath/$page.$langtwo");
        copy("$onepath/$page.wiki.$langone", "$outpath/$page.wiki.$langone");
        copy("$twopath/$page.wiki.$langtwo", "$outpath/$page.wiki.$langtwo");

        if ($dot == 79) {
            print "\n";
            $dot = 0;
        }

        $dot++;
        print ".";
    }
}

close(list);
print "\nNumber of pairs copied : ".$numpairs."\n";
