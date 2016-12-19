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
