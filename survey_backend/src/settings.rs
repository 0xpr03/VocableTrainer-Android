use config::*;
use failure::Fallible;

#[derive(Debug, Deserialize)]
pub struct Database {
    pub user: String,
    pub password: String,
    pub host: String,
    pub port: u16,
    pub database: String,
}

#[derive(Debug, Deserialize)]
pub struct Settings {
    pub database: Database,
    pub log: Log,
    pub files: Files,
    pub bind: Bind,
}

#[derive(Debug, Deserialize)]
pub struct Files {
    pub static_files_dir: String,
}

#[derive(Debug, Deserialize)]
pub struct Log {
    pub requests: bool,
}

#[derive(Debug, Deserialize)]
pub struct Bind {
    pub host: String,
    pub port: u16,
}

impl Settings {
    pub fn new() -> Fallible<Self> {
        let mut s = Config::new();
        s.merge(File::with_name("config/default.toml"))?;

        s.merge(File::with_name("config/local.toml").required(false))?;

        s.merge(Environment::with_prefix("survey"))?;

        let s = s.try_into()?;
        Ok(s)
    }
}
