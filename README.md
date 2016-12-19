Components
----------
### Recognizer
The Thrift recognizer included in this project can be used like so:

```java
String path = getClass().getResource("/myfile.thrift").getFile();
String idl = readFile(path, StandardCharsets.UTF_8);
ParsingResult<?> result = ThriftParser.apply(idl);

if (result.matched()) {
    // Thrift file is valid
}
```

### AST (Abstract Syntax Tree)
Given the following file:


```thrift
namespace java com.mitchseymour.thrift.testmonkey

include "gorillas.thrift"

const string FOREST_NAME = "The Mystical Forest"
const bool INHABITABLE = true
const i32 POPULATION = 14203
const i64 DAYS_UNTIL_DISCOVERY = 92233
const list<string> FRUITS_AVAILABLE = ["bananas", "mangoes", "kiwi"]
const list<i64> FOREST_SECTIONS = [1,2]
const map<string,i32> FRUIT_IDS = {"banana" : 1,"mango": 2}

typedef string MonkeyType

enum Family { LESSER_API ANCIENT_APE }
enum Activity { SWIMMING, CLIMBING, SWINGING }
enum ForestAnimal { BIRD, MOUSE, TIGER }
enum WaterAnimal { FISH, ALLIGATOR }

struct Monkey {
    1: string name,
    2: i32 age,
    3: Family ancestry,
    4: list<string> offspring,
    5: map<string,bool> food_likes,
    6: set<Activity> favorite_activities,
    7: BestFriend bff,
}

union BestFriend {
    1: ForestAnimal forest_animal,
    2: WaterAnimal water_animal,
}

exception TooTired {
    1: string message;
}

service Chimp {
    void call(),
    bool is_munching(),
    bool do_activity(1:Activity activity) throws (1:TooTired tooTired),
}

```

We can build an AST like so:

```java
Optional<DocumentNode> parsedDocument = parseThriftFileAst("/testmonkey.thrift");
assert(parsedDocument.isPresent());
DocumentNode document = parsedDocument.get();
System.out.println(document.printTree());
```

The above example will output:

```bash
    Document:
      Header:
        Include: gorillas.thrift
      Header:
        General Namespace: java com.mitchseymour.thrift.gorillas
      Definition:
        Const: string FOREST_NAME
      Definition:
        Const: bool INHABITABLE
      Definition:
        Const: i32 POPULATION
      Definition:
        Const: i64 DAYS_UNTIL_DISCOVERY
      Definition:
        Const: ListTypeNode FRUITS_AVAILABLE
      Definition:
        Const: ListTypeNode FOREST_SECTIONS
      Definition:
        Const: MapTypeNode FRUIT_IDS
      Definition:
        Type Def: string  MonkeyType
      Definition:
        Enum: Family
          Enum Value: ANCIENT_APE
          Enum Value: LESSER_API
      Definition:
        Enum: Activity
          Enum Value: SWINGING
          Enum Value: CLIMBING
          Enum Value: SWIMMING
      Definition:
        Enum: ForestAnimal
          Enum Value: TIGER
          Enum Value: MOUSE
          Enum Value: BIRD
      Definition:
        Enum: WaterAnimal
          Enum Value: ALLIGATOR
          Enum Value: FISH
      Definition:
        Struct: Monkey
          Field: 1: string name
          Field: 2: i32 age
          Field: 3: Family ancestry
          Field: 4: ListTypeNode offspring
          Field: 5: MapTypeNode food_likes
          Field: 6: SetTypeNode favorite_activities
          Field: 7: BestFriend bff
      Definition:
        Union: BestFriend
          Field: 2: WaterAnimal water_animal
          Field: 1: ForestAnimal forest_animal
      Definition:
        Exception: TooTired
      Definition:
        Service: Chimp
          Function: call
            Arguments:
          Function: is_munching
            Arguments:
          Function: do_activity
            Arguments:
              Field: 1: Activity activity
            Throws:
              Field: 1: TooTired tooTired
      Definition:
        Struct: Gorilla
          Field: 1: string name
          Field: 2: i32 age
```
