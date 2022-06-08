use anyhow::Result;
use config::*;

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
    pub fn new() -> Result<Self> {
        let s = Config::builder()
            .add_source(File::with_name("config/default.toml"))
            .add_source(File::with_name("config/local.toml").required(false))
            .add_source(Environment::with_prefix("survey"))
            .build()?
            .try_deserialize()?;

        Ok(s)
    }
}
