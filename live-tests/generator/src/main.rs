/*Copyright 2018 Aron Heinecke <aron.heinecke@t-online.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
extern crate clap;
extern crate rand;

use std::fs::metadata;
use std::path::PathBuf;
use std::fs::File;
use std::io::Write;
use std::io::BufWriter;
use std::time::{Instant,Duration};
use rand::prelude::*;
use clap::{Arg,App};

fn main() {
    let matches = App::new("generator")
        .version("0.1.0")
        .author("Aron Heinecke <aron.heinecke@t-online.de>")
        .about("create VT-A test lists")
        .arg(Arg::with_name("invalid")
            .short("i")
            .long("invalid")
            .value_name("percentage")
            .takes_value(true)
            .validator(is_correct_percentage)
            .help("Create a random amount of invalid entries which are too short or long.
Specify amount in 1 - 100%."))
        .arg(Arg::with_name("file")
            .short("f")
            .long("file")
            .takes_value(true)
            .value_name("FILE")
            .validator(verify_path)
            .default_value("list.csv")
            .help("output file or full path"))
        .arg(Arg::with_name("rawlist")
            .long("rawlist")
            .takes_value(false)
            .help("create a raw data list"))
        .arg(Arg::with_name("lists")
            .short("l")
            .long("lists")
            .conflicts_with("rawlist")
            .takes_value(true)
            .value_name("amount")
            .validator(is_uint)
            .default_value("1")
            .help("amount of lists, 1 per default"))
        .arg(Arg::with_name("verbose")
            .short("v")
            .long("verbose")
            .takes_value(false)
            .help("verbose output"))
        .arg(Arg::with_name("elements")
            .short("e")
            .long("elements")
            .value_name("amount")
            .conflicts_with("random")
            .takes_value(true)
            .validator(is_uint)
            .help("amount of elements per list, 10 per default"))
        .arg(Arg::with_name("random")
            .short("r")
            .long("random")
            .value_name("max")
            .takes_value(true)
            .validator(is_uint)
            .help("use random amount of elements per list"))
        .get_matches();
    
    let file = matches.value_of("file").unwrap();
    let rawlist = matches.is_present("rawlist");
    let lists_amount = matches.value_of("lists").unwrap().parse::<u32>().unwrap();
    let mut elements = if matches.is_present("elements") {
        matches.value_of("elements").unwrap().parse::<u32>().unwrap()
    } else {
        10
    };
    
    let random = matches.is_present("random");
    let max = if random {
        matches.value_of("random").unwrap().parse::<u32>().unwrap()
    } else {
        20
    };
    
    let create_invalid = matches.is_present("invalid");
    let invalid_percent = if create_invalid {
        matches.value_of("invalid").unwrap().parse::<f64>().unwrap()
    } else {
        0.0
    };
    
    let path = get_path(file).unwrap();
    println!("Using file {:?}",path);
    let file = match File::create(path) {
        Ok(f) => f,
        Err(e) => return println!("Can't create or truncate file: {}",e)
    };
    let mut buffer = BufWriter::new(file);
    
    let mut rng = thread_rng();
    
    let mut invalid_type_flip = false;
    let start = Instant::now();
    for i in 0..lists_amount {
        if !rawlist {
            writeln!(&mut buffer, "TABLE\\,//INFO,//START//,").unwrap();
            writeln!(&mut buffer, "T Multi{0:04},MColA{0:04},MColB{0:04},",i).unwrap();
        }
        if random {
            elements = rng.gen_range(0, max);
        }
        for j in 0..elements {
            if create_invalid && rng.gen_bool(invalid_percent / 100.0) {
                if invalid_type_flip {
                    writeln!(&mut buffer, "A{l:04}-{r:04}",l = i,r=j).unwrap();
                } else {
                    writeln!(&mut buffer, 
                    "A{l:04}-{r:04},B{l:04}-{r:04},Tip{l:04}-{r:04},Addition{l:04}-{r:04},Noise{l:04}-{r:04}",
                    l = i,r=j).unwrap();
                }
                invalid_type_flip = !invalid_type_flip;
            } else {
                writeln!(&mut buffer, 
                "A{l:04}-{r:04},B{l:04}-{r:04},Tip{l:04}-{r:04},Addition{l:04}-{r:04}",
                l = i,r=j).unwrap();
            }
        }
    }
    buffer.flush().unwrap();
    let elapsed = start.elapsed();
    println!("Finished in {}{:04}ms",elapsed.as_secs(),elapsed.subsec_millis());
}

fn verify_path(input: String) -> Result<(),String> {
    match get_path(&input) {
        Ok(_) => Ok(()),
        Err(e) => Err(e)
    }
}

/// Get path for input if possible
fn get_path(input: &str) -> Result<PathBuf,String> {
    let path_o = PathBuf::from(input);
    let path;
    if path_o.parent().is_some() && path_o.parent().unwrap().is_dir() {
        path = path_o;
    } else {
        let mut path_w = std::env::current_dir().unwrap();
        path_w.push(input);
        path = path_w;
    }
    
    if path.is_dir() {
        return Err(format!("Specified file is a directory {:?}",path));
    }
    
    if path.exists() {
        match metadata(&path) {
            Err(e) => return Err(format!("Unable to verify path: {:?} {}",path,e)),
            Ok(m) => {
                if m.permissions().readonly() {
                    return Err(String::from("Can't write to file!"));
                }
            }
        }
    }
    
    Ok(path)
}

/// verify function for u8 1-100 percentage input
fn is_correct_percentage(v: String) -> Result<(), String> {
    if let Ok(percentage) = v.parse::<u8>() {
        if percentage <= 100 && percentage > 0 {
            Ok(())
        } else if percentage == 0 {
            Err(String::from("Levave out this option if you don't want it to activate"))
        } else {
            Err(String::from("Percentage not between 1 and 100"))
        }
    } else {
        Err(String::from("Value is not a correct positive percentage value between 1 and 100"))
    }
}

/// verify function for u32 input
fn is_uint(v: String) -> Result<(), String> {
    if v.parse::<u32>().is_ok() {
        Ok(())
    } else {
        Err(String::from("Value is not a positive number"))
    }
}
