#[macro_use]
extern crate serde_derive;
#[macro_use]
extern crate log;

use actix_files::Files;
use actix_web::{middleware::Logger, web, App, HttpServer};
use env_logger;
use mysql_async::{Opts, OptsBuilder, Pool};

mod db;
mod get;
mod post;
mod settings;

use get::*;
use post::*;
use settings::Settings;

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    if std::env::var("RUST_LOG").is_err() {
        std::env::set_var("RUST_LOG", "actix_web=debug,survey_backend=debug");
    }
    env_logger::init();

    let settings = match settings::Settings::new() {
        Ok(v) => v,
        Err(e) => {
            error!("{}", e);
            panic!("{}", e);
        }
    };

    let pool = init_pool(&settings).await;

    HttpServer::new(move || {
        App::new()
            .app_data(web::Data::new(pool.clone()))
            .service(api_post)
            .service(api_get)
            .service(Files::new("/", "./fs").index_file("index.html"))
            .wrap(Logger::default())
    })
    .backlog(8192)
    .keep_alive(None)
    .bind(format!("{}:{}", settings.bind.host, settings.bind.port))
    .expect("Can't bind server!")
    .run()
    .await?;
    Ok(())
}

async fn init_pool(settings: &Settings) -> Pool {
    trace!("Initializing DB");
    let builder = OptsBuilder::default();
    let opts: Opts = builder
        .ip_or_hostname(settings.database.host.clone())
        .db_name(Some(settings.database.database.clone()))
        .user(Some(settings.database.user.clone()))
        .pass(Some(settings.database.password.clone()))
        .tcp_keepalive(Some(60_000 * 5 as u32))
        .tcp_port(settings.database.port.clone())
        .into();
    let pool = Pool::new(opts);
    {
        // check connection
        let _conn = match pool.get_conn().await {
            Ok(v) => v,
            Err(e) => {
                error!("Can't connect to DB: {:?}", e);
                panic!("Can't connect to DB: {:?}", e);
            }
        };
    }
    info!("DB initialized");
    pool
}
