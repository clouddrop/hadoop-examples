package samar.wordcount;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RawLocalFileSystem;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class Chaining {

	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

		private static int counter = 0;

		public TokenizerMapper() {
			counter++;
		}

		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();

		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			System.out.println("I am here Mapper1 " + counter);
			StringTokenizer itr = new StringTokenizer(value.toString());
			while (itr.hasMoreTokens()) {
				word.set(itr.nextToken());
				context.write(word, one);
			}
		}

	}

	public static class TokenizerMapper2 extends Mapper<Object, Text, Text, IntWritable> {

		private static int counter = 0;

		public TokenizerMapper2() {
			counter++;
		}

		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();

		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			System.out.println("I am here Mapper2 " + counter);
			StringTokenizer itr = new StringTokenizer(value.toString());
			while (itr.hasMoreTokens()) {
				word.set(itr.nextToken());
				context.write(word, one);
			}
		}
	}

	public static class IntSumCombiner extends Reducer<Text, IntWritable, Text, IntWritable> {
		private static int counter = 0;
		private IntWritable result = new IntWritable();

		public IntSumCombiner() {
			counter++;
		}

		@Override
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

			System.out.println("I am here Combiner " + counter + " " + this);
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}
	}

	public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

		private static int counter = 0;
		private IntWritable result = new IntWritable();

		public IntSumReducer() {
			counter++;
		}

		@Override
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

			System.out.println("I am here Reducer " + counter);
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}
	}

	public static class IntSumReducer2 extends Reducer<Text, IntWritable, Text, IntWritable> {

		private static int counter = 0;
		private IntWritable result = new IntWritable();

		public IntSumReducer2() {
			counter++;
		}

		@Override
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

			System.out.println("I am here Reducer2 " + counter);
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		conf.setStrings("fs.default.name", "hdfs://localhost:54310");
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}

		Job job1 = new Job(conf, "word count1");

		job1.setJarByClass(WordCount.class);

		job1.setMapperClass(TokenizerMapper.class);
		job1.setCombinerClass(IntSumCombiner.class);
		job1.setReducerClass(IntSumReducer.class);

		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(IntWritable.class);

		job1.setInputFormatClass(TextInputFormat.class);
		job1.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job1, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job1, new Path("file:///tmp/inter/"));

		ControlledJob cJob1 = new ControlledJob(conf);
		cJob1.setJob(job1);

		Job job2 = new Job(conf, "word count1");

		job2.setJarByClass(WordCount.class);

		job2.setMapperClass(TokenizerMapper.class);
		job2.setCombinerClass(IntSumCombiner.class);
		job2.setReducerClass(IntSumReducer.class);

		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(IntWritable.class);

		job2.setInputFormatClass(TextInputFormat.class);
		job2.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job2, new Path("file:///tmp/inter/part*"));
		FileOutputFormat.setOutputPath(job2, new Path(otherArgs[1]));

		ControlledJob cJob2 = new ControlledJob(conf);
		cJob2.setJob(job2);

		JobControl jobctrl = new JobControl("jobctrl");
		jobctrl.addJob(cJob1);
		jobctrl.addJob(cJob2);
		cJob2.addDependingJob(cJob1);

		Thread jobRunnerThread = new Thread(new JobRunner(jobctrl));
		jobRunnerThread.start();

		while (!jobctrl.allFinished()) {
			System.out.println("Still running...");
			Thread.sleep(5000);
		}
		System.out.println("done");
		jobctrl.stop();

		// Cleaning intermediate data.. can be ignored. ch
		FileSystem fs = new RawLocalFileSystem();
		fs.delete(new Path("/tmp/inter/part*"), true);
		fs.close();

	}
}

class JobRunner implements Runnable {
	private JobControl control;

	public JobRunner(JobControl _control) {
		this.control = _control;
	}

	public void run() {
		this.control.run();
	}
}
