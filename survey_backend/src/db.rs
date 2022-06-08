use anyhow::Result;
use chrono::NaiveDate;
use mysql_async::{from_row, prelude::*, Pool};

use crate::post::SurveyPostData;

pub async fn survey_add(pool: &Pool, msg: SurveyPostData) -> Result<()> {
    let mut conn = pool.get_conn().await?;
    conn.exec_drop(
        "INSERT INTO `apis` (`time`,`api`) VALUES(NOW(),?)",
        (msg.api.clone(),),
    )
    .await?;
    Ok(())
}

pub async fn survey_overview(pool: &Pool, from: Option<NaiveDate>) -> Result<Vec<SurveyGetEntry>> {
    let mut conn = pool.get_conn().await?;
    let mut request = match from {
        Some(v) => {
            conn.exec_iter(
                r#"SELECT ma.api
                , COUNT(1) AS total
                , COUNT(1) / t.cnt * 100 AS `percentage`
            FROM apis ma
            CROSS JOIN (SELECT COUNT(1) AS cnt FROM apis mai
            WHERE mai.time > ?) t
            WHERE ma.time > ?
            GROUP BY ma.api
            ORDER BY ma.api DESC;"#,
                vec![v, v],
            )
            .await?
        }
        None => {
            conn.exec_iter(
                "SELECT ma.api
                , COUNT(1) AS total
                , COUNT(1) / t.cnt * 100 AS `percentage`
            FROM apis ma
            CROSS JOIN (SELECT COUNT(1) AS cnt FROM apis) t
            GROUP BY ma.api
            ORDER BY ma.api DESC;",
                (),
            )
            .await?
        }
    };
    let data: Vec<SurveyGetEntry> = request
        .map(|result| {
            let (api, _total, perc) = from_row::<(i32, i32, f64)>(result);
            SurveyGetEntry {
                api,
                percentage: perc,
            }
        })
        .await?;

    Ok(data)
}

// pub struct DbExecutor(pub Pool);

// impl Message for SurveyPostData {
//     type Result = Fallible<SurveyPostData>;
// }

// impl Message for SurveyGetData {
//     type Result = Fallible<Vec<SurveyGetEntry>>;
// }

#[derive(Debug, Serialize, Deserialize)]
pub struct SurveyGetEntry {
    pub api: i32,
    pub percentage: f64,
}

// impl Actor for DbExecutor {
//     type Context = SyncContext<Self>;
// }

// impl Handler<SurveyPostData> for DbExecutor {
//     type Result = Fallible<SurveyPostData>;

//     fn handle(&mut self, msg: SurveyPostData, _: &mut Self::Context) -> Self::Result {
//         self.0
//             .prep_exec(
//                 "INSERT INTO `apis` (`time`,`api`) VALUES(NOW(),?)",
//                 (msg.api.clone(),),
//             )
//             .map_err(|e| BackendError::DBQuery(e))?;
//         Ok(msg)
//     }
// }

// impl Handler<SurveyGetData> for DbExecutor {
//     type Result = Fallible<Vec<SurveyGetEntry>>;

//     fn handle(&mut self, msg: SurveyGetData, _: &mut Self::Context) -> Self::Result {
//         match self.handle_fallible(msg) {
//             Err(e) => {
//                 eprintln!("{}", e);
//                 Err(e)
//             }
//             Ok(v) => Ok(v),
//         }
//     }
// }

// impl DbExecutor {
//     fn handle_fallible(&mut self, msg: SurveyGetData) -> Fallible<Vec<SurveyGetEntry>> {
//         let request = match msg.from {
//             Some(v) => self.0.prep_exec(
//                 r#"SELECT ma.api
//                     , COUNT(1) AS total
//                     , COUNT(1) / t.cnt * 100 AS `percentage`
//                 FROM apis ma
//                 CROSS JOIN (SELECT COUNT(1) AS cnt FROM apis mai
//                 WHERE mai.time > ?) t
//                 WHERE ma.time > ?
//                 GROUP
//                     BY ma.api;"#,
//                 (v, v),
//             )?,
//             None => self.0.prep_exec(
//                 "SELECT ma.api
//                     , COUNT(1) AS total
//                     , COUNT(1) / t.cnt * 100 AS `percentage`
//                 FROM apis ma
//                 CROSS JOIN (SELECT COUNT(1) AS cnt FROM apis) t
//                 GROUP
//                     BY ma.api;",
//                 (),
//             )?,
//         };
//         let data: Fallible<Vec<SurveyGetEntry>> = request
//             .map(|result| {
//                 let (api, _total, perc) = from_row_opt::<(i32, i32, f64)>(result?)?;
//                 Ok(SurveyGetEntry {
//                     api,
//                     percentage: perc,
//                 })
//             })
//             .collect();
//         data
//     }
// }
