use config;
use mysql;

#[derive(Fail, Debug)]
pub enum BackendError {
    #[fail(display = "Config read error {}", _0)]
    ConfigError(#[cause] config::ConfigError),
    #[fail(display = "Error performing db query: {}", _0)]
    DBQuery(#[cause] mysql::error::Error),
}
