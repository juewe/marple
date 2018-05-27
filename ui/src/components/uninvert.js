import React, { PropTypes } from 'react';
import { Nav, NavItem, FormGroup, FormControl, Radio, Form, Button, Table } from 'react-bootstrap';

import { loadUninvertFieldsData } from '../data';

const TERMSLISTSTYLE = {
    marginTop: '10px',
    marginLeft: '15px'
};

class Uninvert extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            docidPrefix: "",
            postingUninvertList: []
        };
        this.onError = this.onError.bind(this);
        this.loadAndDisplayData = this.loadAndDisplayData.bind(this);
        this.setDocidFilter = this.setDocidFilter.bind(this);
    }

    onError(errmsg) {
    }

    loadAndDisplayData(docidFilter) {
        const onSuccess = data => {
            this.setState({ postingUninvertList: data, docidPrefix: docidFilter });
        };

        loadUninvertFieldsData(this.props.segment, this.props.field, docidFilter, onSuccess, this.onError);
    }

    setDocidFilter(docidFilter) {
        // TODO: add validation check
        this.loadAndDisplayData(docidFilter);
    }

    render() {
        const s = this.state;

        console.log(s);

        const postingUninvertList = s.postingUninvertList.map(data =>
            <div>{data.docid}: {data.text}</div>
        );

        return <div>
            <Form inline onSubmit={ e => e.preventDefault() }>
                <FormGroup>
                    <FormControl type="text" value={s.docidPrefix}
                                 placeholder={'Filter by docid'}
                                 onChange={ e => this.setDocidFilter(e.target.value) }
                                 style={{width:'90%'}} />
                </FormGroup>
            </Form>
            {postingUninvertList}
        </div>;
    }
}

Uninvert.propTypes = {
    segment: PropTypes.oneOfType([
        PropTypes.string, PropTypes.number
    ]),
    field: PropTypes.string.isRequired,
};

export default Uninvert;
