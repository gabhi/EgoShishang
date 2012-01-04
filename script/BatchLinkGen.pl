#!/usr/bin/perl
#
use strict; 
use warnings;

my @host_list = ("irkm-1","irkm-2","irkm-3","irkm-4","irkm-5","irkm-6");

my $jobName = "PageLink";
my $data_root = "/soe/manazhao/projects/EgoShishang/data";
my $input_fp = $data_root."/"."book_leaf";
my $split_root = "$data_root/$jobName";
-d $split_root || mkdir $split_root;

my $script_root = "/soe/manazhao/projects/EgoShishang/script";
my $taskCmd = "perl $script_root/GenerateCrawlUrl.pl   ";

-f $input_fp || die "input big file does not exists $!";
-d $split_root || mkdir $split_root;
my $split_cnt = scalar @host_list;
my @split_fh_arr = ();

open INPUTFILE, "<$input_fp" || die $!;

my @input_fp_fields = split "/", $input_fp;

my $input_name = $input_fp_fields[$#input_fp_fields];
my @split_fp_arr = ();

for(my $i = 0; $i < $split_cnt; $i++)
{
	my $tmp_fh;
	my $split_fp = "$split_root/$input_name"."_split$i";
	open $tmp_fh, ">$split_fp" || die $!;
	push @split_fh_arr, $tmp_fh;
	push @split_fp_arr, $split_fp;
}

while(<INPUTFILE>)
{
	my  $fh_idx = int(rand($split_cnt));
	my $tmp_fh = $split_fh_arr[$fh_idx];
	print $tmp_fh $_;
}

foreach(@split_fh_arr)
{
	close $_;
}

for(my $i = 0; $i < $split_cnt; $i++)
{
	my $in_fp = $split_fp_arr[$i];
	my $output_fp = $in_fp."_res";
	my $log_fp = $in_fp."_log";
	my $cur_host = $host_list[$i];
	my $remote_cmd = "ssh $cur_host  'nohup $taskCmd $in_fp $output_fp 1 > $log_fp 2>&1 &' ";
	print $remote_cmd."\n";
	`$remote_cmd`;
	
}
