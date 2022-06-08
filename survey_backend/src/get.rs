use actix_web::*;
use chrono::naive::NaiveDate;
use mysql_async::Pool;

use crate::db;

#[derive(Debug, Deserialize)]
pub struct SurveyGetData {
    pub from: Option<NaiveDate>,
}

#[get("/data/get/api")]
pub async fn api_get(
    req: web::Query<SurveyGetData>,
    state: web::Data<Pool>,
) -> Result<HttpResponse> {
    let p: &Pool = &**state;
    match db::survey_overview(p, req.from).await {
        Err(e) => {
            error!("Failed reading survey data: {}", e);
            Ok(HttpResponse::InternalServerError().finish())
        }
        Ok(v) => Ok(HttpResponse::Ok().json(v)),
    }
}
