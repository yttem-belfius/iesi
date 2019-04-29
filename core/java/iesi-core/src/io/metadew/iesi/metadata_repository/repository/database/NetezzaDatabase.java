package io.metadew.iesi.metadata_repository.repository.database;

import io.metadew.iesi.metadata.definition.MetadataField;
import io.metadew.iesi.metadata_repository.repository.database.connection.NetezzaDatabaseConnection;

import java.util.Optional;

public class NetezzaDatabase extends Database{

    String schema;


    public NetezzaDatabase(NetezzaDatabaseConnection databaseConnection, String schema) {
        super(databaseConnection);
        this.schema = schema;
    }

    @Override
    public String getSystemTimestampExpression() {
        return "CURRENT_TIMESTAMP";
    }

    @Override
    public String getAllTablesQuery(String pattern) {
        return "select SCHEMA as \"OWNER\", TABLENAME as \"TABLE_NAME\" from _V_TABLE where"
                + getSchema().map(schema -> " OWNER = '" + schema + "' and").orElse("")
                + " TABLENAME like '"
                + pattern
                + "%' order by TABLENAME asc";
    }

	@Override
	public String createQueryExtras() {
		return "";
	}

	@Override
	public boolean addComments() {
		return true;
	}

	@Override
	public String toQueryString(MetadataField field) {
		StringBuilder fieldQuery = new StringBuilder();
		// Data Types
		switch (field.getType()) {
			case "string":
				fieldQuery.append("VARCHAR (").append(field.getLength()).append(" CHAR)");
				break;
			case "flag":
				fieldQuery.append("CHAR (").append(field.getLength()).append(")");
				break;
			case "number":
				fieldQuery.append("NUMERIC");
				break;
			case "timestamp":
				fieldQuery.append("TIMESTAMP");
				break;
		}

		// Default DtTimestamp
		if (field.getDefaultTimestamp().trim().equalsIgnoreCase("y")) {
			fieldQuery.append(" DEFAULT CURRENT_TIMESTAMP");
		}

		// Nullable
		if (field.getNullable().trim().equalsIgnoreCase("n")) {
			fieldQuery.append(" NOT NULL");
		}
		return fieldQuery.toString();
	}

	public Optional<String> getSchema() {
        return Optional.ofNullable(schema);
    }

}