use mysql;

#[derive(Fail, Debug)]
pub enum BackendError {
    #[fail(display = "Error performing db query: {}", _0)]
    DBQuery(#[cause] mysql::error::Error),
}
