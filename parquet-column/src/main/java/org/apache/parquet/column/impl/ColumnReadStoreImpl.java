/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.parquet.column.impl;

import org.apache.parquet.VersionParser;
import org.apache.parquet.VersionParser.ParsedVersion;
import org.apache.parquet.VersionParser.VersionParseException;
import org.apache.parquet.column.ColumnDescriptor;
import org.apache.parquet.column.ColumnReadStore;
import org.apache.parquet.column.ColumnReader;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.column.page.PageReader;
import org.apache.parquet.io.api.Converter;
import org.apache.parquet.io.api.GroupConverter;
import org.apache.parquet.io.api.PrimitiveConverter;
import org.apache.parquet.schema.GroupType;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.Type;

/**
 * Implementation of the ColumnReadStore
 *
 * Initializes individual columns based on schema and converter
 *
 * @author Julien Le Dem
 *
 */
public class ColumnReadStoreImpl implements ColumnReadStore {

  private final PageReadStore pageReadStore;
  private final GroupConverter recordConverter;
  private final MessageType schema;
  private final ParsedVersion writerVersion;
  private boolean hasFlatSchema;
  private boolean allFieldsRequired;

  /**
   * @param pageReadStore underlying page storage
   * @param recordConverter the user provided converter to materialize records
   * @param schema the schema we are reading
   */
  public ColumnReadStoreImpl(PageReadStore pageReadStore,
                             GroupConverter recordConverter,
                             MessageType schema, String createdBy) {
    super();
    this.pageReadStore = pageReadStore;
    this.recordConverter = recordConverter;
    this.schema = schema;

    this.hasFlatSchema = true;
    this.allFieldsRequired = true;

    for (int i = 0; i < schema.getFieldCount(); i += 1) {
      Type field = schema.getFields().get(i);
      if (!field.isPrimitive() || field.isRepetition(Type.Repetition.REPEATED)) {
        this.hasFlatSchema = false;
      }
      if (!field.isRepetition(Type.Repetition.REQUIRED)) {
        this.allFieldsRequired = false;
      }
    }

    ParsedVersion version;
    try {
      version = VersionParser.parse(createdBy);
    } catch (RuntimeException e) {
      version = null;
    } catch (VersionParseException e) {
      version = null;
    }
    this.writerVersion = version;
  }

  @Override
  public ColumnReader getColumnReader(ColumnDescriptor path) {
    if (hasFlatSchema) {
      return newFlatMemColumnReader(path, pageReadStore.getPageReader(path), allFieldsRequired);
    } else {
      return newMemColumnReader(path, pageReadStore.getPageReader(path));
    }
  }

  private ColumnReaderImpl newMemColumnReader(ColumnDescriptor path, PageReader pageReader) {
    PrimitiveConverter converter = getPrimitiveConverter(path);
    return new ColumnReaderImpl(path, pageReader, converter, writerVersion);
  }

  private FlatColumnReaderImpl newFlatMemColumnReader(ColumnDescriptor path, PageReader pageReader, boolean allFieldsRequired) {
    PrimitiveConverter converter = getPrimitiveConverter(path);
    return new FlatColumnReaderImpl(path, pageReader, converter, writerVersion, allFieldsRequired);
  }

  private PrimitiveConverter getPrimitiveConverter(ColumnDescriptor path) {
    Type currentType = schema;
    Converter currentConverter = recordConverter;
    for (String fieldName : path.getPath()) {
      final GroupType groupType = currentType.asGroupType();
      int fieldIndex = groupType.getFieldIndex(fieldName);
      currentType = groupType.getType(fieldName);
      currentConverter = currentConverter.asGroupConverter().getConverter(fieldIndex);
    }
    return currentConverter.asPrimitiveConverter();
  }
}
