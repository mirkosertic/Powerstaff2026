# Beispielkonfiguration für JDBC Metadaten-Extraktion im MCPLuceneServer

```
lucene:
    metadata:
        jdbc:
            enabled: true
            url: "jdbc:mysql://localhost:3306/powerstaff?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Berlin"
            username: "powerstaff"
            password: "powerstaff"              # env-var substitution supported
            poolSize: 5
            connectionTimeout: 30000            # ms
            queryTimeout: 5000                  # ms
            query: |
                SELECT JSON_OBJECT(
                    'fields', JSON_ARRAY(
                        JSON_OBJECT('name', 'tagessatz', 'type', 'long', 'value', f.salary_per_day_long, 'faceted', CAST(FALSE AS JSON)),
                        JSON_OBJECT('name', 'code', 'type', 'keyword', 'value', f.code, 'faceted', CAST(FALSE AS JSON)),
                        JSON_OBJECT('name', 'tags',        'type', 'long', 'values', (
                            SELECT JSON_ARRAYAGG(ft.tag_id)
                                FROM freelancer_tags ft
                            WHERE ft.freelancer_id = f.id
                        ), 'faceted', CAST(FALSE AS JSON))
                    )
                ) AS metadata_json
                FROM
                    freelancer f
                WHERE
                    f.code = REGEXP_SUBSTR(:file_path, '[A-Z]+-[0-9]+')
            parameters:
                - name: file_path
                  sourceField: file_path        # Lucene field to use as query parameter
            json:
                columnName: metadata_json       # Column in the result set containing the JSON

            # Optional: background sync when DB metadata changes
            sync:
                enabled: false
                intervalMinutes: 5
                query: |
                  SELECT code AS dbmeta_code
                  FROM freelancer
                  WHERE creation_date > :last_sync_timestamp OR creation_date > :last_sync_timestamp                
