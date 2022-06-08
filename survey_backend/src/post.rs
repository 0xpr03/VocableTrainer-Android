use crate::db;
use actix_web::*;
use mysql_async::Pool;

#[derive(Debug, Serialize, Deserialize)]
pub struct SurveyPostData {
    pub api: i32,
}

#[post("/data/add/api")]
pub async fn api_post(
    req: web::Json<SurveyPostData>,
    state: web::Data<Pool>,
) -> Result<HttpResponse> {
    let p: &Pool = &**state;
    match db::survey_add(p, req.into_inner()).await {
        Err(e) => {
            error!("Failed storing survey data: {}", e);
            Ok(HttpResponse::InternalServerError().finish())
        }
        Ok(v) => Ok(HttpResponse::Ok().json(v)),
    }
}
