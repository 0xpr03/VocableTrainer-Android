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

#[derive(Debug, Serialize, Deserialize)]
pub struct SurveyGetEntry {
    pub api: i32,
    pub percentage: f64,
}