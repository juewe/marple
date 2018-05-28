import React from 'react';
import ReactDOM from 'react-dom';
import { Nav, NavItem, Col, Tabs, Tab, Alert, Grid, Row } from 'react-bootstrap';

import MarpleNav from './components/marplenav';
import SegmentView from './components/segmentview';
import { Segments } from './components/misc';
import { setEndpoint, segmentFilter, loadIndexData, loadFieldsData } from './data';


class MarpleContent extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      indexData: { indexpath: "loading", generation: -1, segments: []},
      fieldsData: [],
      endpoint: "http://localhost:8080"
    };

    this.selectSegment = this.selectSegment.bind(this);
    this.showAlert = this.showAlert.bind(this);
    this.dismissAlert = this.dismissAlert.bind(this);
    this.updateEndpoint = this.updateEndpoint.bind(this);
  }

  componentDidMount() {
    setEndpoint(this.state.endpoint);
    loadIndexData(data => {
      this.setState({ indexData: data });
    }, errorMsg => this.showAlert(errorMsg, true))
  }

  updateEndpoint(e) {
    setEndpoint(e.target.value);
    this.setState({ endpoint: e.target.value });
    loadIndexData(data => {
      this.setState({ indexData: data });
    }, errorMsg => this.showAlert(errorMsg, true))
  }

  selectSegment(selectedSegment) {
      this.setState({ selectedSegment });
  }

  showAlert(message, isError) {
    this.setState({
      alertMessage: message,
      alertLevel: isError ? 'danger' : 'warning'
    })
  }

  dismissAlert() {
    this.setState({
      alertLevel: undefined
    });
  }

  render() {
    const s = this.state;
    const alert = s.alertLevel ? <Grid><Row><Alert
      bsStyle={s.alertLevel} onDismiss={this.dismissAlert}>
      <strong>{s.alertLevel == 'danger' ? 'ERROR: ' : 'Warning: '}</strong>
      {s.alertMessage}</Alert></Row></Grid> : '';

    return (<div>
      <MarpleNav indexData={s.indexData} endpoint={s.endpoint} updateEndpoint={this.updateEndpoint}/>
      { alert }
      <Col md={2} style={{ paddingBottom: "80px" }}>
        <Segments segments={s.indexData.segments}
                  onSelect={this.selectSegment}
                  selected={s.selectedSegment}/>
      </Col>
        <SegmentView indexData={s.indexData} showAlert={this.showAlert} segment={s.selectedSegment}/>
    </div>);

  }
}

ReactDOM.render(<MarpleContent/>, document.getElementById("content"));
