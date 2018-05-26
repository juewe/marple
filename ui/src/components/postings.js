import React, { PropTypes } from 'react';
import { Button, FormGroup, FormControl, Form } from 'react-bootstrap';
import { loadPostings } from '../data';
import PostingItem from './postingitem';

const FETCH_COUNT = 50;


class Postings extends React.Component {
    constructor(props) {
        super(props);
        this.state = { postings: [], docidPrefix: "" };
        this.componentDidMount = this.componentDidMount.bind(this);
        this.loadMore = this.loadMore.bind(this);
        this.setDocidPrefix = this.setDocidPrefix.bind(this);
    }

    componentDidMount() {
        const p = this.props;
        loadPostings(p.segment, p.field, p.term, p.encoding, "", 0, FETCH_COUNT,
            data => {
	            this.setState(data);
	        },
	        errmsg => {
                if (errmsg.includes('No term')) {
                    this.setState({ postings: [] });
                }
                else {
                    this.props.showAlert(errmsg, true);
                }
	        }
        );
    }

    loadMore() {
        const p = this.props;
        const s = this.state;

        loadPostings(p.segment, p.field, p.term, p.encoding, "", s.moreFrom, FETCH_COUNT,
            data => {
                this.setState({
                    postings: this.state.postings.concat(data.postings),
                    moreFrom: data.moreFrom
                });
  	        },
  	        errmsg => {
                if (errmsg.includes('No term')) {
                    this.setState({ postings: [] });
                }
                else {
                    this.props.showAlert(errmsg, true);
                }
            }
        );
    }

    setDocidPrefix(docidPrefix) {
        console.log("docid " + docidPrefix);
        const p = this.props;
        loadPostings(p.segment, p.field, p.term, p.encoding, docidPrefix, 0, FETCH_COUNT,
            data => {
	            this.setState({ postings: data.postings, docidPrefix: docidPrefix });
	        },
	        errmsg => {
                if (errmsg.includes('No term')) {
                    this.setState({ postings: [] });
                }
                else {
                    this.props.showAlert(errmsg, true);
                }
	        }
        );
    }

    render() {
        const s = this.state;
        const p = this.props;

        if (s.postings.length == 0) {
            return <div></div>;
        }

        const moreFromLink = s.moreFrom ?
            <Button bsStyle="primary" bsSize="xsmall"
             style={{ marginTop: "3px" }}
             onClick={this.loadMore}>Load more</Button> : '';

        const postingList = s.postings.map((docid, idx) =>
            <PostingItem key={idx} segment={p.segment} field={p.field}
                term={p.term} encoding={p.encoding} docid={docid}
                showAlert={p.showAlert}/>
        );
        return <div>
            <div style={{ color: 'grey' }}>in docs:</div>
            <Form inline>
                <FormGroup>
                    <FormControl type="text" value={s.docidPrefix}
                                             placeholder={'Filter by prefix'}
                                             onChange={ e => this.setDocidPrefix(e.target.value) }
                                             style={{width:'90%'}} />
                </FormGroup>
            </Form>
            {postingList}
            {moreFromLink}
        </div>;
    }
}

Postings.propTypes = {
  segment: PropTypes.oneOfType([
    PropTypes.string, PropTypes.number
  ]),
  field: PropTypes.string.isRequired,
  term: PropTypes.string.isRequired,
  encoding: PropTypes.string.isRequired,
  showAlert: PropTypes.func.isRequired
}

export default Postings;
