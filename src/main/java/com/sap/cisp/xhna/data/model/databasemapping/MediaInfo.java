package com.sap.cisp.xhna.data.model.databasemapping;

import java.util.Map;

public class MediaInfo {
    private String file_path;
    private String schema;
    private String table;
    private String partitioned;
    private String partition_column;

    public String getFilesystem_path() {
        return file_path;
    }

    public String getDatabase_schema() {
        return schema;
    }

    public String getDatabase_table() {
        return table;
    }

    public String getPartitioned() {
        return partitioned;
    }

    public String getPartition_column() {
        return partition_column;
    }

    @Override
    public String toString() {
        return "MediaInfo [file_path=" + file_path + ", schema=" + schema
                + ", table=" + table + ", partitioned=" + partitioned
                + ", partition_column=" + partition_column + "]";
    }

    public MediaInfo(String file_path, String schema, String table,
            String partitioned, String partition_column) {
        this.file_path = file_path;
        this.schema = schema;
        this.table = table;
        this.partitioned = partitioned;
        this.partition_column = partition_column;
    }

    public static MediaInfo exetractInfoFromSet(Map<String, String> map) {
        // TODO Auto-generated method stub
        MediaInfo info = new MediaInfo(map.get("hdfs_path"),
                map.get("hive_schema"), map.get("hive_table"),
                map.get("partitioned"), map.get("partition_column"));
        return info;
    }

}
