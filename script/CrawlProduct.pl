#!/usr/bin/perl
use strict;
use warnings;
use LWP::Simple;

my $productPat = '<div class="productTitle"><a href="([^"]+?)">(.*?)<\/a>';
my $asinPat = '\/dp\/([\d\w]+)';

while(<STDIN>)
{
	chomp;
	my ($cateId, $url, $pageCnt) = split "\t";
	my $page = get $url;
	my $tries = 10;
	for(; $tries > 0 && !$page; $tries--)
	{
		$page = get $url;
	}
	if(not $page) 
	{
		print STDERR $url."\n";
		next;
	}
	while( $page =~ /$productPat/gs)
	{
		my ($prodUrl, $prodTitle) = ($1,$2);
		#extract the asin number
		my $asin = -1;
		if($prodUrl =~ /$asinPat/)
		{
			$asin = $1;
		}
		print join("\t",($cateId, $asin, $prodTitle, $prodUrl));
		print "\n";
	}
	last;
}
