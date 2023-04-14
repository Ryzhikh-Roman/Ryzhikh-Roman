import React, { Fragment } from 'react';
import Button from './Button';
//import axios from "axios"

const Sandbox = () => (

  <Fragment>

    <h2><span>1. Post request:</span></h2>
    <Button onClick={() => { 
      console.log('!!!!')
      fetch("http://localhost:5000/hello", {
        'method':'POST',
        'headers':{
          'Content-Type':'application/json'
        },
        'body':JSON.stringify({'Hello':['1','2']})
      })
      .then(res => res.json())
      .then((data) => {
        console.log(data)
      })
      .catch((error) => {
        console.log(error)
      })
    }}>Post</Button>

    <h2><span>2. Get request:</span></h2>
    <Button onClick={() => { 
      console.log('!!!!');
      fetch("http://localhost:5000/members")
      .then(res => res.json())
      .then((data) => {
        console.log(data)
      })
      .catch((err) => {
        console.log(err)
      })
      }}>Get</Button>

  </Fragment>
);

export default Sandbox;