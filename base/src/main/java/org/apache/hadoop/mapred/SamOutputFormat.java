/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.mapred;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FSDataOutputStream;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.util.*;

/** 
 * An {@link OutputFormat} that writes plain text files. 
 */
@InterfaceAudience.Public
@InterfaceStability.Stable
public class SamOutputFormat<K, V> extends FileOutputFormat<K, V> {

  protected static class SamRecordWriter<K, V>
    implements RecordWriter<K, V> {
    private static final String utf8 = "UTF-8";
    private static final byte[] newline;
    static {
      try {
        newline = "\n".getBytes(utf8);
      } catch (UnsupportedEncodingException uee) {
        throw new IllegalArgumentException("can't find " + utf8 + " encoding");
      }
    }

    protected DataOutputStream out;
    private final byte[] keyValueSeparator;

    public SamRecordWriter(DataOutputStream out, String keyValueSeparator) {
      this.out = out;
      try {
        this.keyValueSeparator = keyValueSeparator.getBytes(utf8);
      } catch (UnsupportedEncodingException uee) {
        throw new IllegalArgumentException("can't find " + utf8 + " encoding");
      }
    }

    public SamRecordWriter(DataOutputStream out) {
      this(out, "\t");
    }

    /**
     * Write the object to the byte stream, handling Text as a special
     * case.
     * @param o the object to print
     * @throws IOException if the write throws, we pass it on
     */
    private void writeObject(Object o) throws IOException {
      if (o instanceof Text) {
        Text to = (Text) o;
        out.write(to.getBytes(), 0, to.getLength());
      } else {
        out.write(o.toString().getBytes(utf8));
      }
    }

    public synchronized void write(K key, V value)
      throws IOException {

      boolean nullKey = key == null || key instanceof NullWritable;
      boolean nullValue = value == null || value instanceof NullWritable;
      if (nullKey && nullValue) {
        return;
      }
      String samRecord = "";
      if (!nullKey)  samRecord = key.toString();
      if (!nullValue) samRecord = samRecord + "\t" + value.toString();
      
	  String keyWrite = "";
	  String[] ss = samRecord.split("\t");
	  if (samRecord.startsWith("@") || ss.length < 5) {
		  keyWrite = "1" + samRecord;
	  } else {
		  if (ss[2].equals("*")) {
			  keyWrite = "u";
		  } else {
			  keyWrite = "m";
		  }
		  keyWrite += ss[2] + "@" + ss[3] + "@" + ss[0];
      }
	  out.write(keyWrite.toString().getBytes(utf8));
	  out.write(keyValueSeparator);
	  out.write(samRecord.toString().getBytes(utf8));
      out.write(newline);
    }

    public synchronized void close(Reporter reporter) throws IOException {
      out.close();
    }
  }

  public RecordWriter<K, V> getRecordWriter(FileSystem ignored,
                                                  JobConf job,
                                                  String name,
                                                  Progressable progress)
    throws IOException {
    boolean isCompressed = getCompressOutput(job);
    String keyValueSeparator = job.get("mapreduce.output.textoutputformat.separator", 
                                       "\t");
    if (!isCompressed) {
      Path file = FileOutputFormat.getTaskOutputPath(job, name);
      FileSystem fs = file.getFileSystem(job);
      FSDataOutputStream fileOut = fs.create(file, progress);
      return new SamRecordWriter<K, V>(fileOut, keyValueSeparator);
    } else {
      Class<? extends CompressionCodec> codecClass =
        getOutputCompressorClass(job, GzipCodec.class);
      // create the named codec
      CompressionCodec codec = ReflectionUtils.newInstance(codecClass, job);
      // build the filename including the extension
      Path file = 
        FileOutputFormat.getTaskOutputPath(job, 
                                           name + codec.getDefaultExtension());
      FileSystem fs = file.getFileSystem(job);
      FSDataOutputStream fileOut = fs.create(file, progress);
      return new SamRecordWriter<K, V>(new DataOutputStream
                                        (codec.createOutputStream(fileOut)),
                                        keyValueSeparator);
    }
  }
}

