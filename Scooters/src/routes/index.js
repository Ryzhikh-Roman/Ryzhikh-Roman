var express = require('express');
var router = express.Router();
const fs = require('fs');

let aggregated = require('../public/aggregated')

var importFlag = false;
let neo4j = require('neo4j-driver')
const alert = require("alert");
var uri = "bolt://localhost:7687"
var user = "neo4j"
var password = "0000"
//const driver = neo4j.driver(uri, neo4j.auth.basic(user, password), { disableLosslessIntegers: true })

var url = 'bolt://neo4j:7687';
var driver = neo4j.driver(url, { disableLosslessIntegers: true });

session = driver.session()

async function getBd(){
  let result = await session.run(
      'MATCH (a) RETURN a SKIP 0 LIMIT 100'
  )
  return result
}

async function getClients(){
  let users = []
  let result = await session.run(
      'MATCH (a:USER) RETURN a SKIP 0 LIMIT 100'
  )
  for (let i in result.records){
    users.push(result.records[i].get(0).properties)
  }
  return users
}

async function getScooters(){
  let scooters = []
  let result = await session.run(
      'MATCH (a:SCOOTER) RETURN a SKIP 0 LIMIT 100'
  )
  for (let i in result.records){
    scooters.push(result.records[i].get(0).properties)
  }
  return scooters
}

async function getWarehouses(){
  let warehouses = []
  let result = await session.run(
      'MATCH (a:WAREHOUSE) RETURN a SKIP 0 LIMIT 100'
  )
  for (let i in result.records){
    warehouses.push(result.records[i].get(0).properties)
  }
  return warehouses
}

async function getTrips(){
  let trips = []
  let result = await session.run(
      'MATCH (a:TRIP) RETURN a SKIP 0 LIMIT 100'
  )
  for (let i in result.records){
    trips.push(result.records[i].get(0).properties)
  }
  return trips
}

async function getUnloadingAreas(){
  let unloading_areas = []
  let result = await session.run(
      'MATCH (a:UNLOADING_AREA) RETURN a SKIP 0 LIMIT 100'
  )
  for (let i in result.records){
    unloading_areas.push(result.records[i].get(0).properties)
  }
  return unloading_areas
}

/* GET home page. */
router.get('/', async function(req, res) {
  if (importFlag === false)
  {
    importFlag = true
    let file = require('../exported/bd.json');
    await session.run(
        'match(a) detach delete a'
    )
    //console.log(file.nodes)
    for (let i in file.nodes)
    {
      //console.log(file.nodes[i].labels)
      let str = JSON.stringify(file.nodes[i].properties).replace(/{"/g, '{').replace(/,"/g, ',').replace(/":/g, ':')
      await session.run(
          'create (:' + file.nodes[i].labels[0]  + str + ')'
      )
    }
    for (let i in file.relationships){
      //console.log(file.relationships[i].type, ' ', file.relationships[i].start, ' ', file.relationships[i].end)
      await session.run(
          'MATCH (n)\n' +
          'WHERE id(n) = $n\n' +
          'MATCH (c)\n' +
          'WHERE id(c) = $c\n' +
          'create (n)-[:' + file.relationships[i].type + ']->(c)',
          {n:file.relationships[i].start, c:file.relationships[i].end}
      )
      //console.log(file.relationships[i])
    }
  }
  let cookies = req.cookies;
  if (!cookies.type) {
    res.render('login_page', {title: 'Login'});
  } else {
    res.redirect('/main')
  }
});

router.get('/main', (req, res) => {
  let type = req.cookies.type;
  if (type) {
    res.render('main_page', {title: "Main", type: type});
  } else {
    res.redirect('/');
  }
});

router.get('/tariffs', (req, res) => {
  res.render('tariffs', {title: 'Tariffs'})
});

router.get('/rules', (req, res) => {
  res.render('rules', {title: 'Rules'})
});

router.get('/dbs', async (req, res) => {
  let users = await getClients()
  let scooters = await getScooters()
  let warehouses = await getWarehouses()
  let unloading_areas = await getUnloadingAreas()
  //console.log(warehouses)
  let type = req.cookies.type;
  if (type === 'admin') {
    res.render('dbs', {title: 'Data Bases', users: users, scooters: scooters, warehouses: warehouses, unloading_area: unloading_areas})
  } else if (type === 'user') {
    res.redirect('/main');
  } else {
    res.redirect('/')
  }
});

router.get('/enterLogin', async (req, res) => {
  let user_type = ''
  let user_id = ''
  let login = req.query.login;
  let password = req.query.password;
  let result = await session.run(
      'match (user:USER {login: $login, password: $password})  \n' +
      'return user SKIP 0 LIMIT 100',
      {login: login, password: password}
  )
  if (result.records.length > 0)
  {
    result = result.records[0].get(0).properties
    user_type = result.type;
    user_id = result.phone;
    res.cookie("type", user_type);
    res.cookie("user id", user_id);
  }
  //console.log(user_type)
  if (user_type === 'admin' || user_type === 'user'){
    res.redirect('/main')
  } else {
    alert('Incorrect login or password');
    res.redirect('/')
  }
})

router.get('/aggregated', (req, res) => {
  let type = req.cookies.type;
  if (type === 'admin') {
    //console.log("var = ", req.query.variant);
    let variant = req.query.variant;
    if (variant === '0'){
      let keys = Object.keys(aggregated[0][0])
      res.render('table', {title: 'Пользователи, которые больше всего ломают самокаты', keys: keys, data: aggregated[0]})
    } else if (variant === '1') {
      let keys = Object.keys(aggregated[1][0])
      res.render('table', {title: 'Пользователи, у которых давно не было поздок', keys: keys, data: aggregated[1]})
    } else {
      let keys = Object.keys(aggregated[2][0])
      res.render('table', {title: 'Самые далекие от складов самокаты', keys: keys, data: aggregated[2]})
    }
  } else if (type === 'user') {
    res.redirect('/main')
  } else {
    res.redirect('/')
  }
});

router.get('/clients', async (req, res) => {
  let users = await getClients()
  //getBd()
  let keys = users.length > 0 ? Object.keys(users[0]): ['name', 'password', 'login', 'type', 'phone']
  //console.log(keys)
  let type = req.cookies.type;
  if (type === 'admin') {
    res.render('table', {title: 'Пользователи', keys: keys, data: users});
  } else if (type === 'user') {
    res.redirect('/main')
  } else {
    res.redirect('/')
  }
  //отделить клиентов от админов? Нужно ли выводить пароли?
});

router.get('/scooters', async (req, res) => {
  let scooters = await getScooters()
  //console.log(scooters)
  let keys = scooters.length > 0 ? Object.keys(scooters[0]): ['number', 'battery', 'coordinate_x', 'coordinate_y', 'status'];
  //console.log(keys)
  let type = req.cookies.type;
  if (type === 'admin') {
    res.render('table', {title: 'Самокаты', keys: keys, data: scooters});
  } else if (type === 'user') {
    res.redirect('/main')
  } else {
    res.redirect('/')
  }
});

router.get('/warehouses', async (req, res) => {
  let warehouses = await getWarehouses()
  let keys = ['address', 'coordinate_x', 'coordinate_y', 'capacity'];
  let type = req.cookies.type;
  if (type === 'admin') {
    res.render('table', {title: 'Склады', keys: keys, data: warehouses});
  } else if (type === 'user') {
    res.redirect('/main')
  } else {
    res.redirect('/')
  }
});

router.get('/unloading_area', async (req, res) => {
  let unloading_areas = await getUnloadingAreas()
  let keys = unloading_areas.length > 0 ? Object.keys(unloading_areas[0]): ['address', 'coordinate_x', 'coordinate_y', 'number'];
  let type = req.cookies.type;
  if (type === 'admin') {
    res.render('table', {title: 'Площадки выгрузки', keys: keys, data: unloading_areas});
  } else if (type === 'user') {
    res.redirect('/main')
  } else {
    res.redirect('/')
  }
});

router.get('/trips', async (req, res) => {
  let trips = await getTrips()
  let keys = trips.length > 0 ? Object.keys(trips[0]) : ['cost', 'time_end', 'time_start', 'status'];
  let type = req.cookies.type;
  if (type === 'admin') {
    res.render('table', {title: 'Поездки', keys: keys, data: trips});
  } else if (type === 'user') {
    res.redirect('/main')
  } else {
    res.redirect('/')
  }
});

router.get('/exit', (req, res) => {
  res.clearCookie('type');
  res.clearCookie('user id');
  res.redirect('/')
});

async function attachedToScooters(scooter){
  let attachedToScooters = []
  let result = await session.run(
      'match (sc:SCOOTER{number: $number})-[net]-(node) \n' +
      'return sc,net,node SKIP 0 LIMIT 100',
      {number: scooter}
  )
  for (let i in result.records){
    let scooter = result.records[i].get(0).properties

    let relationship = result.records[i].get(1).type //HAS_NOW/TALKS_ABOUT/USED

    let attachedTo = result.records[i].get(2)

    let dict = {'scooter': scooter.number + '(scooter)', 'relationship': relationship, 'attachedTo': attachedTo}
    attachedToScooters.push(dict)
  }

  return attachedToScooters
}

async function attachedToUsers(user){
  let attachedToUsers = []
  let result = await session.run(
      'match (us:USER{login: $login})-[net]-(node) \n' +
      'return us,net,node SKIP 0 LIMIT 100',
      {login: user}
  )
  for (let i in result.records){
    let user = result.records[i].get(0).properties.login

    let relationship = result.records[i].get(1).type //HAS_NOW/TALKS_ABOUT/USED

    let attachedTo = result.records[i].get(2)

    let dict = {'user': user + '(user)', 'relationship': relationship, 'attachedTo': attachedTo}
    attachedToUsers.push(dict)
  }

  return attachedToUsers
}

async function attachedToWarehouses(warehouse){
  let attachedToWarehouses = []
  let result = await session.run(
      'match (wr:WAREHOUSE{number: $number})-[net]-(node) \n' +
      'return wr,net,node SKIP 0 LIMIT 100',
      {number: warehouse}
  )
  for (let i in result.records){
    let warehouse = result.records[i].get(0).properties.number

    let relationship = result.records[i].get(1).type //HAS_NOW/TALKS_ABOUT/USED

    let attachedTo = result.records[i].get(2)

    let dict = {'warehouse': warehouse + '(warehouse)', 'relationship': relationship, 'attachedTo': attachedTo}
    attachedToWarehouses.push(dict)
  }

  return attachedToWarehouses
}

async function attachedToUnloadingAreas(unloading_area){
  let attachedToUnloadingAreas = []
  let result = await session.run(
      'match (ua:UNLOADING_AREA{number: $number})-[net]-(node) \n' +
      'return ua,net,node SKIP 0 LIMIT 100',
      {number: unloading_area}
  )
  for (let i in result.records){
    let unloading_area = result.records[i].get(0).properties.number

    let relationship = result.records[i].get(1).type //HAS_NOW/TALKS_ABOUT/USED

    let attachedTo = result.records[i].get(2)

    let dict = {'unloading_area': unloading_area + '(unloading area)', 'relationship': relationship, 'attachedTo': attachedTo}
    attachedToUnloadingAreas.push(dict)
  }

  return attachedToUnloadingAreas
}

router.get('/free-choice', async (req, res) => {
  let relationships = []

  if (req.query.scooters)
  {
    if (typeof(req.query.scooters) == 'string')
    {
      relationships.push(await attachedToScooters(req.query.scooters))
    }
    else
    {
      for (let i in req.query.scooters)
      {
        relationships.push(await attachedToScooters(req.query.scooters[i]))
      }
    }
    //Поправить!!!
  }

  if (req.query.users)
  {
    if (typeof(req.query.users) == 'string')
    {
      relationships.push(await attachedToUsers(req.query.users))
    }
    else
    {
      for (let i in req.query.users)
      {
        relationships.push(await attachedToUsers(req.query.users[i]))
      }
    }
  }

  if (req.query.warehouses)
  {
    if (typeof(req.query.warehouses) == 'string')
    {
      relationships.push(await attachedToWarehouses(req.query.warehouses))
    }
    else
    {
      for (let i in req.query.warehouses)
      {
        relationships.push(await attachedToWarehouses(req.query.warehouses[i]))
      }
    }
  }

  if (req.query.unloading_areas)
  {
    if (typeof(req.query.unloading_areas) == 'string')
    {
      relationships.push(await attachedToUnloadingAreas(req.query.unloading_areas))
    }
    else
    {
      for (let i in req.query.unloading_areas)
      {
        relationships.push(await attachedToUnloadingAreas(req.query.unloading_areas[i]))
      }
    }
  }
  let keys = ['node1', 'relationship','node2']
  res.render('filter_table', {title: 'Фильтры', keys: keys, data: relationships})
});

router.get('/add_scooter', async (req, res) =>
{
  let warehouses = await getWarehouses();
  res.render('add_scooter', {warehouses: warehouses})
})

async function addEditScooter(info){
  //console.log(+info.number)
  let result = await session.run(
      'MERGE (a:SCOOTER {number: TOINTEGER($number)}) <- [net:TALKS_ABOUT] - (tech:TECH_CARD)\n' +
      'ON CREATE SET a.battery = 100, a.coordinate_x = 10, a.coordinate_y = 10, a.status = $status, ' +
      'tech.creationYear = $creation_year, tech.manufacturer = $manufacturer, tech.maxPowerCapacity = $max_power_cap, tech.mileage = $mileage\n' +
      'ON MATCH SET a.status = $status,' +
      'tech.mileage = $mileage\n' +
      'return a, net, tech',
      {number: parseInt(info.number), creation_year: +info.creation_year, manufacturer: info.manufacturer,
        max_power_cap: info.max_power_cap, mileage: +info.mileage, status: info.status}
  )
  return result
}

router.get('/add_edit_scooter', async (req, res) =>
{
  //console.log(req.query)
  await addEditScooter(req.query)
  res.redirect('/add_scooter')
})

router.post('/filter/:title', async (req, res) =>
{
  //console.log(req.body)
  let title = req.params.title
  //console.log("title = ", title)
  let result = '';
  let keys = '';
  switch (title)
  {
    case 'Пользователи':
      result = await session.run(
        'match (user:USER) \n' +
        'WHERE toLower(user.name) CONTAINS toLower($name) and toLower(user.password) CONTAINS toLower($password) and toLower(user.login) CONTAINS toLower($login) and ' +
        'toLower(user.type) CONTAINS toLower($type) and toLower(user.phone) CONTAINS toLower($phone)\n' +
        'return user SKIP 0 LIMIT 100',
        {name: req.body.name, password: req.body.password, login: req.body.login, type: req.body.type, phone: req.body.phone}
    );
      let users = [];
      for (let i in result.records)
      {
        users.push(result.records[i].get(0).properties)
      }
      //console.log("users = ", users)
      keys = users.length > 0 ? Object.keys(users[0]): ['name', 'password', 'login', 'type', 'phone'];
      res.render('table', {title: title, keys: keys,data: users});
      break;
    case 'Самокаты':
      let start_number = req.body.start_number === ''? -1: +req.body.start_number-1;
      let stop_number = req.body.stop_number === ''? Number.MAX_SAFE_INTEGER: +req.body.stop_number+1;
      let start_battery = req.body.start_battery === '' ? -1: +req.body.start_battery-1;
      let stop_battery = req.body.stop_battery === ''? 101: +req.body.stop_battery+1;
      let start_coord_x = req.body.start_coordinate_x === ''? -181: +req.body.start_coordinate_x-1;
      let stop_coord_x = req.body.stop_coordinate_x === ''? 181: +req.body.stop_coordinate_x+1;
      let start_coord_y = req.body.start_coordinate_y === ''? -91: +req.body.start_coordinate_y-1;
      let stop_coord_y = req.body.stop_coordinate_y === ''? 91: +req.body.stop_coordinate_y+1;
      let status = req.body.status;

      result = await session.run(
          'match (sc:SCOOTER) \n' +
          'WHERE sc.number > $start_number and sc.number < $stop_number and ' +
          'sc.battery > $start_battery and sc.battery < $stop_battery and ' +
          'sc.coordinate_x > $start_coord_x and sc.coordinate_x < $stop_coord_x and ' +
          'sc.coordinate_y > $start_coord_y and sc.coordinate_y < $stop_coord_y and ' +
          'toLower(sc.status) CONTAINS toLower($status)\n' +
          'return sc SKIP 0 LIMIT 100',
          {start_number: start_number, stop_number: stop_number, start_battery: start_battery, stop_battery: stop_battery, start_coord_x: start_coord_x,
            stop_coord_x: stop_coord_x, start_coord_y: start_coord_y, stop_coord_y: stop_coord_y, status:status}
      );

      let scooters = [];
      for (let i in result.records)
      {
        scooters.push(result.records[i].get(0).properties)
      }
      keys = scooters.length > 0 ? Object.keys(scooters[0]): ['number', 'battery', 'coordinate_x', 'coordinate_y', 'status'];
      res.render('table', {title: title, keys: keys, data: scooters});
      break;
    case 'Склады':

      //console.log(req.body)
      let address2 =  req.body.address;
      let coord_x_start = req.body.start_coordinate_x === ''? -181: +req.body.start_coordinate_x-1;
      let coord_x_end = req.body.stop_coordinate_x === ''? 181: +req.body.stop_coordinate_x+1;
      let coord_y_start = req.body.start_coordinate_y === ''? -91: +req.body.start_coordinate_y-1;
      let coord_y_end = req.body.stop_coordinate_y === ''? 91: +req.body.stop_coordinate_y+1;
      let capacity1 = req.body.start_capacity === '' ? -1 : +req.body.start_capacity-1;
      let capacity2 = req.body.stop_capacity === '' ? Number.MAX_SAFE_INTEGER : +req.body.stop_capacity+1;
      result = await session.run(
          'match (wh:WAREHOUSE) \n' +
          'WHERE toLower(wh.address) contains toLower($address) and\n' +
          'wh.coordinate_x > $cx1 and wh.coordinate_x < $cx2 and \n' +
          'wh.coordinate_y > $cy1 and wh.coordinate_y < $cy2 and \n' +
          'wh.capacity > $cap1 and wh.capacity < $cap2 \n' +
          'return wh SKIP 0 LIMIT 100',
          {address: address2, cx1: coord_x_start, cx2: coord_x_end, cy1: coord_y_start, cy2: coord_y_end, cap1: capacity1, cap2: capacity2}
      );
      let whs = [];
      for (let i in result.records)
      {
        whs.push(result.records[i].get(0).properties)
      }
      keys = ['address', 'coordinate_x', 'coordinate_y', 'capacity'];
      res.render('table', {title: title, keys: keys,data: whs});
      break;
    case 'Площадки выгрузки':
      let coord_x1 = req.body.start_coordinate_x === ''? -181: +req.body.start_coordinate_x-1;
      let coord_x2 = req.body.stop_coordinate_x === ''? 181: +req.body.stop_coordinate_x+1;
      let coord_y1 = req.body.start_coordinate_y === ''? -91: +req.body.start_coordinate_y-1;
      let coord_y2 = req.body.stop_coordinate_y === ''? 91: +req.body.stop_coordinate_y+1;
      let number1 = req.body.start_number === '' ? -1: +req.body.start_number-1;
      let number2 = req.body.stop_number === '' ? Number.MAX_SAFE_INTEGER: +req.body.stop_number+1;
      let address = req.body.address;

      result = await session.run(
          'match (area:UNLOADING_AREA) \n' +
          'WHERE area.number > $minNum and area.number < $maxNum and ' +
          'toLower(area.address) CONTAINS toLower($address) and ' +
          'area.coordinate_x > $start_coord_x and area.coordinate_x < $stop_coord_x and ' +
          'area.coordinate_y > $start_coord_y and area.coordinate_y < $stop_coord_y\n' +
          'return area SKIP 0 LIMIT 100',
          {minNum: number1, maxNum: number2 ,address: address, start_coord_x: coord_x1, stop_coord_x: coord_x2, start_coord_y: coord_y1, stop_coord_y: coord_y2}
      );
      let areas = [];
      for (let i in result.records)
      {
        areas.push(result.records[i].get(0).properties)
      }
      keys = areas.length > 0 ? Object.keys(areas[0]): ['number', 'address', 'coordinate_x', 'coordinate_y'];
      res.render('table', {title: title, keys: keys, data: areas});
      break;
    case 'Поездки':
      let cost1 = req.body.start_cost === '' ? -1: +req.body.start_cost-1;
      let cost2 = req.body.stop_cost === '' ? Number.MAX_SAFE_INTEGER: +req.body.stop_cost+1;
      let time_end1 = req.body.start_time_end === '' ? -1: +req.body.start_time_end-1;
      let time_end2 = req.body.stop_time_end === '' ? Number.MAX_SAFE_INTEGER: +req.body.stop_time_end+1;
      let time_start1 = req.body.start_time_start === '' ? -1: +req.body.start_time_start-1;
      let time_start2 = req.body.stop_time_start === '' ? Number.MAX_SAFE_INTEGER: +req.body.stop_time_start+1;
      let trip_status = req.body.status;

      result = await session.run(
          'match (trip:TRIP) \n' +
          'WHERE trip.cost > $minCost and trip.cost < $maxCost and ' +
          'trip.time_end > $minTimeEnd and trip.time_end < $maxTimeEnd and ' +
          'trip.time_start > $minTimeStart and trip.time_start < $maxTimeStart and ' +
          'toLower(trip.status) contains toLower($status)\n' +
          'return trip SKIP 0 LIMIT 100',
          {minCost: cost1, maxCost: cost2, minTimeEnd: time_end1, maxTimeEnd: time_end2,
          minTimeStart: time_start1, maxTimeStart: time_start2, status: trip_status}
      );

      let trips = [];
      for (let i in result.records)
      {
        trips.push(result.records[i].get(0).properties)
      }
      keys = trips.length > 0 ? Object.keys(trips[0]) : ['cost', 'time_start', 'time_end', 'status'];
      res.render('table', {title: title, keys: keys, data: trips});
      break;
  }

})

router.get('/export', async (req, res) =>
{
  await session.close()
  session = driver.session()

  console.log("export")
  let DB = await getBd();
  let nodes = []
  for (let i in DB.records)
  {
    nodes.push(DB.records[i].get(0))
  }
  //console.log("nodes:\n", nodes)

  let rels = await session.run("match (a)-[b]-(c) where id(a) > id(c) return b");
  //console.log("rels:\n", rels)
  let relationships = []
  for (let i in rels.records)
  {
    relationships.push(rels.records[i].get(0))
  }
  let exprt = {'nodes': nodes, 'relationships': relationships}
  //console.log("relationships:\n", uniqueArray)
  fs.writeFile('src/exported/bd.json', JSON.stringify(exprt, null, 2), function writeJSON(err) {
    if (err) return console.log(err);
  });
  alert('EXPORTED. Файлы лежат в src/exported');
  res.redirect('/dbs')
})

async function importDB(file)
{
  await session.close()
  session = driver.session()

  await session.run(
      'match(a) detach delete a'
  )
  //console.log(file.nodes)
  for (let i in file.nodes)
  {
    //console.log(file.nodes[i].labels)
    let str = JSON.stringify(file.nodes[i].properties).replace(/{"/g, '{').replace(/,"/g, ',').replace(/":/g, ':')
    await session.run(
        'create (:' + file.nodes[i].labels[0]  + str + ')'
    )
  }
  //console.log("RELATIONSHIP IMPORT")
  let str = ''
  let node1 = {}
  let node2 = {}
  for (let i in file.relationships){
    node1 = {}
    node2 = {}
    //console.log(file.relationships[i].type, ' ', file.relationships[i].start, '  ', file.relationships[i].end)
    for (let j in file.nodes)
    {
      if (file.nodes[j].identity === file.relationships[i].start)
      {
        node1 = file.nodes[j]
      }
      if (file.nodes[j].identity === file.relationships[i].end)
      {
        node2 = file.nodes[j]
      }
    }
    str = 'MATCH (n:' + node1.labels[0] + ' ' + JSON.stringify(node1.properties).replace(/{"/g, '{').replace(/,"/g, ',').replace(/":/g, ':') + ')\n' +
        'MATCH (c:' + node2.labels[0] + ' ' + JSON.stringify(node2.properties).replace(/{"/g, '{').replace(/,"/g, ',').replace(/":/g, ':') + ')\n' +
        'create (n)-[:' + file.relationships[i].type + ']->(c)'
    //console.log("str =", str)
    await session.run(
        str
    )
    //console.log(file.relationships[i])
  }
}
router.get('/import', async (req, res) =>
{
  let f = require('../exported/bd.json')
  //console.log("file:\n", f)
  await importDB(f)
  alert('IMPORT');
  res.redirect('/dbs')
})

module.exports = router;
