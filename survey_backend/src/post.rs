use crate::AppState;
use actix_web::*;
use futures::Future;

#[derive(Debug, Serialize, Deserialize)]
pub struct SurveyPostData {
    pub api: i32,
}

pub fn api_post(
    (req, state): (Json<SurveyPostData>, State<AppState>),
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
