#!/usr/local/bin/perl

if ($#ARGV != 3) {
 print "usage: findpairs.pl lang_one lang_two page_path out_path\n";
 exit;
}

$langone = $ARGV[0];
$langtwo = $ARGV[1];
$pagepath = $ARGV[2];
$outpath = $ARGV[3];

print "Collecting pairs for $langone and $langtwo.\n";

opendir(DDIR, "$pagepath/$langone");
@dirs = readdir(DDIR); 

foreach $dir (@dirs) {

  $letter = chop($dir);
  $curone = "$pagepath/$langone/$letter";
  $curtwo = "$pagepath/$langtwo/$letter";

  if ((-d $curone) && (-d $curtwo) && (!($letter eq '.'))) {
    $curout = "$outpath/$letter";
    system "mkdir -p $curout";
    system "perl ./findpairs.pl $langone $langtwo $pagepath $letter $curout";
  }
}

print "Done collecting pairs for $langone and $langtwo.\n";
closedir(DDIR);
