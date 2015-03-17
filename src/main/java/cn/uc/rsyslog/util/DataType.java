package cn.uc.rsyslog.util;

public enum DataType {
	string {
		@Override
		public String getCreateTableSql(String fieldName) {
			return "`"+fieldName + "` varchar(200) DEFAULT NULL";
		}
	},
	char_ {
		@Override
		public String getCreateTableSql(String fieldName) {
			return "`"+fieldName + "` varchar(20) DEFAULT NULL";
		}
	},
	int_ {
		@Override
		public String getCreateTableSql(String fieldName) {
			return "`"+fieldName + "` int(11) DEFAULT 0";
		}

		@Override
		public String changeInsertValue(String value) {
			return value;
		}	
	},
	long_ {
		@Override
		public String getCreateTableSql(String fieldName) {
			return "`"+fieldName + "` bigint(20) DEFAULT 0";
		}

		@Override
		public String changeInsertValue(String value) {
			return value;
		}	
	},
	number {
		@Override
		public String getCreateTableSql(String fieldName) {
			return "`"+fieldName + "` double DEFAULT NULL";
		}

		@Override
		public String changeInsertValue(String value) {
			return value;
		}	
	},
	datetime {
		@Override
		public String getCreateTableSql(String fieldName) {
			return "`"+fieldName + "` datetime DEFAULT NULL";
		}

		@Override
		public String changeInsertValue(String value) {
			return value;
		}
		
	};
	public abstract String getCreateTableSql(String fieldName);
	
	public String changeInsertValue(String value){
		return "'"+value+"'";
	}
}
