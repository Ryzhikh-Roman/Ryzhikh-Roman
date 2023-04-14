import React from 'react';
import { NavLink } from 'react-router-dom';
import './navigation.css';

const Navigation = () => (
  <div className="navigation">
    <div className="logoWrapper">
      <img src="/react.png" height="60" alt="React Image" />
    </div>
    <nav>
      <ul className="nav">
        <li><NavLink exact to="/">Home</NavLink></li>
        <li><NavLink to="/button">Button</NavLink></li>
      </ul>
    </nav>
  </div>
);

export default Navigation;
