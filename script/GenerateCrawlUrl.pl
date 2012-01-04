#!/usr/bin/perl
use strict;
use warnings;
use LWP::Simple;

my $productPat = '<div class="productTitle"><a href="([^"]+?)">(.*?)<\/a>';
my $asinPat = '\/dp\/([\d\w]+)';
#next page link pat : http://www.amazon.cn/gp/search?node=658495051&url=search-alias%3Dbooks#/ref=sr_pg_2?rh=n%3A658390051%2Cn%3A%21658391051%2Cn%3A658393051%2Cn%3A658495051&page=2&ie=UTF8&qid=1325675611

@ARGV >= 2 || die "invlid arguments supplied $!";

-f $ARGV[0] || die "input file does not exist";
open INPUTFILE, "<$ARGV[0]" || die "failed to open input file $!";
open OUTPUTFILE, ">$ARGV[1]" || die "failed to open output file $!";


while(<INPUTFILE>)
{
	chomp;
	my ($cateId, $url, $resultCnt, $pageCnt) = split "\t";
	$pageCnt = 1;
	for(my $pn = 1; $pn <= $pageCnt; $pn++)
	{
		my $pageUrl = "http://www.amazon.cn/s?ie=UTF8&rh=n%3A$cateId&page=$pn";
		if($pn == 1)
		{
			$pageCnt = &getResultCnt($pageUrl);
			$pageCnt = int($pageCnt/12 + 1);
		}
		print OUTPUTFILE join("\t",($cateId, $pageUrl, $pageCnt));
		print OUTPUTFILE "\n";
	}
}


sub getResultCnt
{
	my ($url) = @_;
	my $page_ref = &grabPage($url);
	my $resultCntPat = 'class="resultCount">显示.*?共([\d\,]+)条<\/div>';
	my $resultCnt = 0;
	if( $$page_ref =~ /$resultCntPat/gs)
	{
		$resultCnt = $1;
	}
	$resultCnt =~ s/,//g;
	return $resultCnt;
}


sub grabPage
{
	my ($url) = @_;
	my $tries = 5;
	my $page = undef;
	while($tries > 0 && not defined $page)
	{
		$page = get $url;	
		$tries--;
	}
	defined $page || ($page = "");
	return \$page;
}
