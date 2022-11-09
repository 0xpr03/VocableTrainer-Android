// ignore_for_file: constant_identifier_names

import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';

Future<Database> initDatabase() async {
  String path = await getDatabasesPath();
  Database db = await openDatabase(
    join(path, "voc.db"),
    version: 4,
    onCreate: (db, version) async {
      // clean init
      List<String> steps = [
        _sqlLists,
        _sqlListsIndex,
        _sqlListSync,
        _sqlEntries,
        _sqlEntryIndex,
        _sqlWordsA,
        _sqlWordsAIndex,
        _sqlWordsB,
        _sqlWordsBIndex,
        _sqlSession,
        _sqlSessionMeta,
        _sqlSessionHistory,
        _sqlListsDeleted,
        _sqlListDeletedIndex,
        _sqlEntriesDeleted,
        _sqlEntryDeletedIndex,
        _sqlEntryStats,
        _sqlListCategories,
        _sqlCategory,
        _sqlCategoryIndex,
        _sqlCategoriesDeleted,
        _sqlCategoryDeletedIndex,
        _sqlEntryUsed,
        _sqlEntryUsedIndex,
        _sqlSettings
      ];
      await Future.forEach(steps, (element) async {
        await db.execute(element);
      });
    },
    onUpgrade: (db, oldVersion, newVersion) {
      // upgrade path TODO
    },
  );
  return db;
}

const String _sqlLists = """CREATE TABLE $TBL_LISTS (
    $KEY_NAME_LIST TEXT NOT NULL,
    $KEY_LIST INTEGER PRIMARY KEY,
    $KEY_NAME_A TEXT NOT NULL,
    $KEY_NAME_B TEXT NOT NULL,
    $KEY_CREATED INTEGER NOT NULL,
    $KEY_CHANGED INTEGER NOT NULL,
    $KEY_SHARED INTEGER NOT NULL )""";
const String _sqlListsIndex =
    "CREATE INDEX listChangedI ON $TBL_LISTS ($KEY_CHANGED)";
const String _sqlListSync = """CREATE TABLE $TBL_LIST_SYNC (
    $KEY_LIST INTEGER PRIMARY KEY REFERENCES $TBL_LISTS($KEY_LIST) ON DELETE CASCADE,
    $KEY_LIST_UUID STRING NOT NULL UNIQUE )""";
const String _sqlEntries = """CREATE TABLE $TBL_ENTRIES (
    $KEY_LIST INTEGER NOT NULL REFERENCES $TBL_LISTS($KEY_LIST) ON DELETE CASCADE,
    $KEY_ENTRY INTEGER PRIMARY KEY,
    $KEY_TIP TEXT,
    $KEY_ADDITION TEXT,
    $KEY_CREATED INTEGER NOT NULL,
    $KEY_CHANGED INTEGER NOT NULL,
    $KEY_ENTRY_UUID STRING NOT NULL UNIQUE )""";
const String _sqlEntryIndex =
    "CREATE INDEX entryChangedI ON $TBL_ENTRIES ($KEY_CHANGED)";
const String _sqlEntryUsed = """CREATE TABLE $TBL_ENTRIES_USED(
    $KEY_ENTRY INTEGER PRIMARY KEY REFERENCES $TBL_ENTRIES($KEY_ENTRY) ON DELETE CASCADE,
    $KEY_LAST_USED INTEGER NOT NULL )""";
const String _sqlEntryUsedIndex =
    "CREATE INDEX entryUsedI ON $TBL_ENTRIES_USED ($KEY_LAST_USED)";
const String _sqlWordsA = """CREATE TABLE $TBL_WORDS_A (
    $KEY_ENTRY INTEGER NOT NULL REFERENCES $TBL_ENTRIES($KEY_ENTRY) ON DELETE CASCADE,
    $KEY_MEANING TEXT NOT NULL )""";
const String _sqlWordsB = """CREATE TABLE $TBL_WORDS_B (
    $KEY_ENTRY INTEGER NOT NULL REFERENCES $TBL_ENTRIES($KEY_ENTRY) ON DELETE CASCADE,
    $KEY_MEANING TEXT NOT NULL )""";
const String _sqlWordsAIndex =
    "CREATE INDEX wordsAI ON $TBL_WORDS_A ($KEY_ENTRY)";
const String _sqlWordsBIndex =
    "CREATE INDEX wordsBI ON $TBL_WORDS_B ($KEY_ENTRY)";
const String _sqlSession = """CREATE TABLE $TBL_SESSION (
    $KEY_ENTRY INTEGER PRIMARY KEY REFERENCES $TBL_ENTRIES($KEY_ENTRY) ON DELETE CASCADE,
    $KEY_POINTS INTEGER NOT NULL )""";
const String _sqlSessionMeta = """CREATE TABLE $TBL_SESSION_META (
    $KEY_MKEY TEXT NOT NULL PRIMARY KEY,
    $KEY_MVALUE TEXT NOT NULL )"""; // TODO: replace ?, previously combined primary ?!
const String _sqlSessionHistory = """CREATE TABLE $TBL_SESSION_HISTORY (
    $KEY_DATE INTEGER PRIMARY KEY REFERENCES $TBL_ENTRY_STATS($KEY_DATE) ON DELETE CASCADE )""";
const String _sqlListsDeleted =
    """CREATE TABLE $TBL_LISTS_DELETED ( $KEY_LIST_UUID text NOT NULL PRIMARY KEY,
    $KEY_CREATED INTEGER NOT NULL )""";
const String _sqlListDeletedIndex =
    """CREATE INDEX listDeletedI ON $TBL_LISTS_DELETED ($KEY_CREATED)""";
const String _sqlEntriesDeleted = """CREATE TABLE $TBL_ENTRIES_DELETED (
    $KEY_ENTRY_UUID text NOT NULL PRIMARY KEY,
    $KEY_LIST INTEGER NOT NULL REFERENCES $TBL_LISTS($KEY_LIST) ON DELETE CASCADE,
    $KEY_CREATED INTEGER NOT NULL )""";
const String _sqlEntryDeletedIndex =
    "CREATE INDEX entryDeletedI ON $TBL_ENTRIES_DELETED ($KEY_CREATED)";
const String _sqlEntryStats = """CREATE TABLE $TBL_ENTRY_STATS (
    $KEY_ENTRY INTEGER NOT NULL REFERENCES $TBL_ENTRIES($KEY_ENTRY) ON DELETE CASCADE,
    $KEY_DATE INTEGER PRIMARY KEY,
    $KEY_TIP_NEEDED boolean NOT NULL,
    $KEY_IS_CORRECT boolean NOT NULL )""";
const String _sqlListCategories = """CREATE TABLE $TBL_LIST_CATEGORIES (
    $KEY_LIST INTEGER NOT NULL REFERENCES $TBL_LISTS($KEY_LIST) ON DELETE CASCADE,
    $KEY_CATEGORY INTEGER NOT NULL REFERENCES $TBL_CATEGORY($KEY_CATEGORY) ON DELETE CASCADE,
    "PRIMARY KEY ($KEY_LIST ,$KEY_CATEGORY ))""";
const String _sqlCategory = """CREATE TABLE $TBL_CATEGORY (
    $KEY_CATEGORY INTEGER PRIMARY KEY,
    $KEY_CATEGORY_NAME STRING NOT NULL,
    $KEY_CATEGORY_UUID STRING NOT NULL UNIQUE,
    $KEY_CHANGED INTEGER NOT NULL )""";
const String _sqlCategoryIndex =
    """CREATE INDEX categoryChangedI ON $TBL_CATEGORY ($KEY_CHANGED)""";
const String _sqlCategoriesDeleted = """CREATE TABLE $TBL_CATEGORIES_DELETED (
    $KEY_CATEGORY_UUID text NOT NULL PRIMARY KEY,
    $KEY_CREATED INTEGER NOT NULL )""";
const String _sqlCategoryDeletedIndex =
    "CREATE INDEX categoryDeletedI ON $TBL_CATEGORIES_DELETED ($KEY_CREATED)";
const String _sqlSettings = """CREATE TABLE $TBL_SETTINGS (
    $KEY_SETTINGS_KEY text NOT NULL PRIMARY KEY,
    $KEY_SETTINGS_VALUE text NOT NULL,
    $KEY_CHANGED INTEGER NOT NULL )""";

const TAG = "Database";
const DB_NAME_PRODUCTION = "voc.db";
const MIN_ID_TRESHOLD = 0;
const ID_RESERVED_SKIP = -2;
const TBL_LISTS = "`lists`";
const TBL_LIST_SYNC = "`list_sync`";
const TBL_ENTRIES = "`entries`";
const TBL_SESSION = "`session4`";
const TBL_SESSION_META = "`session_meta`";
const TBL_WORDS_A = "`words_a`";
const TBL_WORDS_B = "`words_b`";
const TBL_SESSION_HISTORY = "`session_stats_history`";
const TBL_LISTS_DELETED = "`lists_deleted`";
const TBL_ENTRIES_DELETED = "`entries_deleted`";
const TBL_ENTRY_STATS = "`entry_stats`";
const TBL_LIST_CATEGORIES = "`categories`";
const TBL_CATEGORY = "`category_name`";
const TBL_CATEGORIES_DELETED = "`categories_deleted`";
const TBL_ENTRIES_USED = "`entries_used`";
const TBL_SETTINGS = "`settings`";
const KEY_ENTRY = "`entry`";
const KEY_NAME_A = "`name_a`";
const KEY_NAME_B = "`name_b`";
const KEY_TIP = "`tip`";
const KEY_TIP_NEEDED = "`tip_needed`";
const KEY_LIST = "`list`";
const KEY_LAST_USED = "`last_used`";
const KEY_NAME_LIST = "`list_name`";
const KEY_MEANING = "`meaning`";
const KEY_CREATED = "`created`";
const KEY_IS_CORRECT = "`is_correct`";
const KEY_SHARED = "`shared`";
//@Deprecated("Deprecated DB version");
const _KEY_CORRECT = "`correct`";
//@Deprecated("Deprecated DB version");
const _KEY_WRONG = "`wrong`";
const KEY_ADDITION = "`addition`";
const KEY_POINTS = "`points`";
const KEY_MKEY = "`key`";
const KEY_MVALUE = "`value`";
const KEY_LIST_UUID = "`uuid_list`";
const KEY_ENTRY_UUID = "`uuid_voc`";
const KEY_CHANGED = "`changed`";
const KEY_DATE = "`date`";
const KEY_CATEGORY = "`category`";
const KEY_CATEGORY_NAME = "`name`";
const KEY_CATEGORY_UUID = "`uuid_cat`";
const KEY_SETTINGS_VALUE = "`settings_v`";
const KEY_SETTINGS_KEY = "`settings_k`";
