#!/usr/bin/perl
use strict;
use warnings;

my $bookRoot = 658391051; 
while(<STDIN>)
{
	my $line = $_;
	chomp;
	my($cid, $cname, $parent, $isCatetoryRoot, $isLeaf, $pathStr) = split "\t";
	my @pathNodes = split '\|', $pathStr;
	my @matchBookRoot = grep { $_ == $bookRoot} @pathNodes; 
	@matchBookRoot > 0 || next;
	if($isLeaf eq "true")
	{
		print $line;
	}
}
