package ni.danny.datax.plugin.writer.oraclewithseqwriter;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordReceiver;
import com.alibaba.datax.common.spi.Writer;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.plugin.rdbms.util.DBUtilErrorCode;
import com.alibaba.datax.plugin.rdbms.util.DataBaseType;
import com.alibaba.datax.plugin.rdbms.writer.Key;

import java.util.List;

public class OracleWithSeqWriter extends Writer {
	private static final DataBaseType DATABASE_TYPE = DataBaseType.Oracle;

	public static class Job extends Writer.Job {
		private Configuration originalConfig = null;
		private OracleWithSeqRdbmsWriter.Job commonRdbmsWriterJob;

		@Override
		public void preCheck() {
            this.init();
            this.commonRdbmsWriterJob.writerPreCheck(this.originalConfig, DATABASE_TYPE);
        }

        @Override
		public void init() {
			this.originalConfig = super.getPluginJobConf();

			String writeMode = this.originalConfig.getString(Key.WRITE_MODE);
			if(null != writeMode && !"INSERT".equals(writeMode.toUpperCase())&&!"UPDATE".equals(writeMode.toUpperCase())){
				throw DataXException
						.asDataXException(
								DBUtilErrorCode.CONF_ERROR,
								String.format(
										"写入模式(writeMode)配置错误. 因为Oracle不支持配置项 writeMode: %s, Oracle只能使用insert/update sql 插入数据. 请检查您的配置并作出修改",
										writeMode));
			}

			this.commonRdbmsWriterJob = new OracleWithSeqRdbmsWriter.Job(
					DATABASE_TYPE);
			this.commonRdbmsWriterJob.init(this.originalConfig);
		}

		@Override
		public void prepare() {
            //oracle实跑先不做权限检查
            //this.commonRdbmsWriterJob.privilegeValid(this.originalConfig, DATABASE_TYPE);
			this.commonRdbmsWriterJob.prepare(this.originalConfig);
		}

		@Override
		public List<Configuration> split(int mandatoryNumber) {
			return this.commonRdbmsWriterJob.split(this.originalConfig,
					mandatoryNumber);
		}

		@Override
		public void post() {
			this.commonRdbmsWriterJob.post(this.originalConfig);
		}

		@Override
		public void destroy() {
			this.commonRdbmsWriterJob.destroy(this.originalConfig);
		}

	}

	public static class Task extends Writer.Task {
		private Configuration writerSliceConfig;
		private OracleWithSeqRdbmsWriter.Task commonRdbmsWriterTask;

		@Override
		public void init() {
			this.writerSliceConfig = super.getPluginJobConf();
			this.commonRdbmsWriterTask = new OracleWithSeqRdbmsWriter.Task(DATABASE_TYPE);
			this.commonRdbmsWriterTask.init(this.writerSliceConfig);
		}

		@Override
		public void prepare() {
			this.commonRdbmsWriterTask.prepare(this.writerSliceConfig);
		}

		@Override
		public void startWrite(RecordReceiver recordReceiver) {
			this.commonRdbmsWriterTask.startWrite(recordReceiver,
					this.writerSliceConfig, super.getTaskPluginCollector());
		}

		@Override
		public void post() {
			this.commonRdbmsWriterTask.post(this.writerSliceConfig);
		}

		@Override
		public void destroy() {
			this.commonRdbmsWriterTask.destroy(this.writerSliceConfig);
		}



	}

}