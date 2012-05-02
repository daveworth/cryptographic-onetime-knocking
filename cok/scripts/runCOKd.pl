#!/usr/bin/perl -w
use strict;

#print "Please enter your keystore password: ";
#system("stty -echo");
#my $keystorepasswd = <STDIN>;
#chomp($keystorepasswd);
#system("stty echo");
#print "\n";

my $PWD = $ENV{'PWD'};
my $JARDIR="$PWD/lib";
my $JPCAP_LIBDIR="$PWD/lib";
my $PCAP_LIBDIR="/usr/lib";

opendir(JARDIR, "$JARDIR");
my @files=readdir(JARDIR);
closedir(JARDIR);

@files = grep /\.jar$/, @files;
foreach (@files){ $ENV{'CLASSPATH'}.=":$JARDIR/$_" };

my $uname = `uname`;
chomp($uname);
if ($uname eq "Darwin") {
	$ENV{'DYLD_LIBRARY_PATH'} = "$PCAP_LIBDIR:$JPCAP_LIBDIR";
} else {
	$ENV{'LD_LIBRARY_PATH'} = "$PCAP_LIBDIR:$JPCAP_LIBDIR";
}

my $ARGSTR = join " ", @ARGV;

print "Running COKd...\n";
exec("java -Djava.rmi.server.codebase=file:$PWD/cokd.jar -Djava.security.policy=$PWD/lib/badPolicy -jar $PWD/cokd.jar $ARGSTR");

