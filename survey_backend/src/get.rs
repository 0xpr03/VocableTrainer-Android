use crate::AppState;
use actix_web::*;
use chrono::naive::NaiveDate;
use futures::Future;

#[derive(Debug, Deserialize)]
pub struct SurveyGetData {
    pub from: Option<NaiveDate>,
}

pub fn api_get(
    (req, state): (Query<SurveyGetData>, State<AppState>),
) -> FutureResponse<HttpResponse> {
    //println!("{:?}", req);
    state
        .db
        .send(req.into_inner())
        .from_err()
        .and_then(|res| match res {
            Ok(v) => Ok(HttpResponse::Ok().json(v)),
            Err(e) => {
                error!("{}", e);
                Ok(HttpResponse::InternalServerError().into())
            }
        })
        .responder()
}
