import React, { PropTypes } from 'react';

import { Navbar, Nav, NavItem, NavDropdown, MenuItem, FormGroup, FormControl, Button } from 'react-bootstrap';

const MarpleNav = props => {
  return <Navbar>
    <Navbar.Header>
    </Navbar.Header>
    <Navbar.Form pullRight>
      <FormGroup>
        <FormControl type="text" placeholder={props.endpoint} onChange={props.updateEndpoint}/>
      </FormGroup>{' '}
    </Navbar.Form>
    <Navbar.Text pullRight>
      Exploring lucene index: {props.indexData.indexpath} ({props.indexData.numDocs} docs/{props.indexData.numDeletedDocs} deletions)
    </Navbar.Text>
  </Navbar>;
};

MarpleNav.propTypes = {
  indexData: PropTypes.object.isRequired
};

export default MarpleNav;
