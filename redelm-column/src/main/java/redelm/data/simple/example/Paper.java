/**
 * Copyright 2012 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package redelm.data.simple.example;

import static redelm.schema.PrimitiveType.Primitive.INT64;
import static redelm.schema.PrimitiveType.Primitive.STRING;
import static redelm.schema.Type.Repetition.OPTIONAL;
import static redelm.schema.Type.Repetition.REPEATED;
import static redelm.schema.Type.Repetition.REQUIRED;
import redelm.data.Group;
import redelm.data.simple.SimpleGroup;
import redelm.schema.GroupType;
import redelm.schema.MessageType;
import redelm.schema.PrimitiveType;

public class Paper {
  public static final MessageType schema =
      new MessageType("Document",
          new PrimitiveType(REQUIRED, INT64, "DocId"),
          new GroupType(OPTIONAL, "Links",
              new PrimitiveType(REPEATED, INT64, "Backward"),
              new PrimitiveType(REPEATED, INT64, "Forward")
              ),
          new GroupType(REPEATED, "Name",
              new GroupType(REPEATED, "Language",
                  new PrimitiveType(REQUIRED, STRING, "Code"),
                  new PrimitiveType(OPTIONAL, STRING, "Country")),
              new PrimitiveType(OPTIONAL, STRING, "Url")));

  public static final MessageType schema2 =
      new MessageType("Document",
          new PrimitiveType(REQUIRED, INT64, "DocId"),
          new GroupType(REPEATED, "Name",
              new GroupType(REPEATED, "Language",
                  new PrimitiveType(OPTIONAL, STRING, "Country"))));

  public static final MessageType schema3 =
      new MessageType("Document",
          new PrimitiveType(REQUIRED, INT64, "DocId"),
          new GroupType(OPTIONAL, "Links",
              new PrimitiveType(REPEATED, INT64, "Backward"),
              new PrimitiveType(REPEATED, INT64, "Forward")
              ));

  public static final SimpleGroup r1 = new SimpleGroup(schema);
  public static final SimpleGroup r2 = new SimpleGroup(schema);
  ////r1
  //DocId: 10
  //Links
  //  Forward: 20
  //  Forward: 40
  //  Forward: 60
  //Name
  //  Language
  //    Code: 'en-us'
  //    Country: 'us'
  //  Language
  //    Code: 'en'
  //  Url: 'http://A'
  //Name
  //  Url: 'http://B'
  //Name
  //  Language
  //    Code: 'en-gb'
  //    Country: 'gb'
  static {
    r1.add("DocId", 10);
    r1.addGroup("Links")
      .append("Forward", 20)
      .append("Forward", 40)
      .append("Forward", 60);
    Group name = r1.addGroup("Name");
    {
      name.addGroup("Language")
        .append("Code", "en-us")
        .append("Country", "us");
      name.addGroup("Language")
        .append("Code", "en");
      name.append("Url", "http://A");
    }
    name = r1.addGroup("Name");
    {
      name.append("Url", "http://B");
    }
    name = r1.addGroup("Name");
    {
      name.addGroup("Language")
        .append("Code", "en-gb")
        .append("Country", "gb");
    }
  }
  ////r2
  //DocId: 20
  //Links
  // Backward: 10
  // Backward: 30
  // Forward:  80
  //Name
  // Url: 'http://C'
  static {
    r2.add("DocId", 20);
    r2.addGroup("Links")
      .append("Backward", 10)
      .append("Backward", 30)
      .append("Forward", 80);
    r2.addGroup("Name")
      .append("Url", "http://C");
  }

  public static final SimpleGroup pr1 = new SimpleGroup(schema2);
  public static final SimpleGroup pr2 = new SimpleGroup(schema2);
  ////r1
  //DocId: 10
  //Name
  //  Language
  //    Country: 'us'
  //  Language
  //Name
  //Name
  //  Language
  //    Country: 'gb'
  static {
    pr1.add("DocId", 10);
    Group name = pr1.addGroup("Name");
    {
      name.addGroup("Language")
        .append("Country", "us");
      name.addGroup("Language");
    }
    name = pr1.addGroup("Name");
    name = pr1.addGroup("Name");
    {
      name.addGroup("Language")
        .append("Country", "gb");
    }
  }

  ////r2
  //DocId: 20
  //Name
  static {
    pr2.add("DocId", 20);
    pr2.addGroup("Name");
  }
}