package com.test.deidentify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.log4j.Logger;

public class DeIdentifyData   {

	static Logger logger = Logger.getLogger(DeIdentifyData.class.getName());
	public static Integer[] encryptCol={2,3,4,5,6,8};
	private static byte[] key1 = new String("samplekey1234567").getBytes();
	
	public static class Map extends MapReduceBase implements
	Mapper<Object, Text, NullWritable, Text> {

		public void map(Object key, Text value,
				OutputCollector<NullWritable, Text> output, Reporter reporter)
		throws IOException {

			//value = PatientID, Name,DOB,Phone Number,Email_Address,SSN,Gender,Disease,weight
			
			 //PatientID, Name,DOB,xxxxx,xxxx@xx.com,SSN,Gender,Disease,weight
				
			StringTokenizer itr = new StringTokenizer(value.toString(),",");
			List<Integer> list=new ArrayList<Integer>();
			Collections.addAll(list, encryptCol);
			//list=2,3,4,5,6,8
			System.out.println("Mapper :: one :"+value);
			String  newStr="";

			int counter=1;

			while (itr.hasMoreTokens()) {
				String token=itr.nextToken(); 
				//System.out.println("token"+token);
				//System.out.println("i="+counter);
				if(list.contains(counter))
				{
					if(newStr.length()>0)
						newStr+=",";

					newStr+=encrypt(token, key1);

				}
				else
				{
					if(newStr.length()>0)
						newStr+=",";
					newStr+=token;
				}	
				counter=counter+1;
			}
			output.collect( NullWritable.get(), new Text(newStr.toString()));
		}
	}


	public static void main(String[] args) throws Exception {
		JobConf conf = new JobConf(DeIdentifyData.class);
		conf.setJobName("DeIdentifyData");

		conf.setOutputKeyClass(NullWritable.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(Map.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		JobClient.runJob(conf);
	}
	

	public static String encrypt(String strToEncrypt, byte[] key)
	{
		try
		{
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			String encryptedString = Base64.encodeBase64String(cipher.doFinal(strToEncrypt.getBytes()));
			return encryptedString.trim();
		}
		catch (Exception e)
		{
			logger.error("Error while encrypting", e);
		}
		return null;

	}	
}