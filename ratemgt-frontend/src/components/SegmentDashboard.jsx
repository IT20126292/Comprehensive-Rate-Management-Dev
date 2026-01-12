import React, { useState, useEffect } from 'react';
import { runSegmentation, fetchSegmentResults, downloadReport } from '../api/api';
import { Button, Table, Alert, Container, Row, Col, Card } from 'react-bootstrap';
import { FaPlay, FaDownload } from 'react-icons/fa';

const Segment = () => {
  const [segments, setSegments] = useState([]);
  const [status, setStatus] = useState('');

  const loadSegments = async () => {
    try {
      const res = await fetchSegmentResults();
      setSegments(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const handleRunSegmentation = async () => {
    try {
      await runSegmentation();
      setStatus('✅ Segmentation executed successfully.');
      await loadSegments();
    } catch (err) {
      setStatus('❌ Failed to run segmentation.');
    }
  };

  const handleDownloadReport = async () => {
    try {
      await downloadReport();
    } catch (err) {
      console.error('Download failed');
    }
  };

  useEffect(() => {
    loadSegments();
  }, []);

  return (
   <Container fluid className="pt-5" style={{ paddingLeft: '300px', backgroundColor: '#f8f9fa', minHeight: '100vh', marginTop:'60px' }}>
      <Row className="mb-4">
        <Col>
          <h5 className="text-primary fw-bold text-center">Customer Segmentation Report Dashboard</h5>
        </Col>
      </Row>

      {status && (
        <Row className="mb-3 justify-content-center">
          <Col md={6}>
            <Alert variant={status.includes('✅') ? 'success' : 'danger'} className="text-center shadow-sm" dismissible>
              {status}
            </Alert>
          </Col>
        </Row>
      )}

      <Row className="mb-4 justify-content-center">
        <Col xs="auto">
          <Button variant="primary" onClick={handleRunSegmentation} className="me-3 shadow-sm rounded-3 px-4 py-2">
            <FaPlay className="me-2" /> Run Segmentation
          </Button>
          <Button variant="success" onClick={handleDownloadReport} className="shadow-sm rounded-3 px-4 py-2">
            <FaDownload className="me-2" /> Download Report
          </Button>
        </Col>
      </Row>

     <Row className="justify-content-center">
        <Col md={10}>
            <Card className="shadow rounded-4 border-0">
            <Card.Header className="bg-primary text-white fw-semibold fs-10 rounded-top">
                Segmentation Results
            </Card.Header>
            <Card.Body className="p-0">
                {/* Add this wrapper for responsiveness */}
               <div
                  className="table-responsive"
                  style={{
                    borderRadius: '0 0 1rem 1rem',
                    overflowX: 'auto',
                    overflowY: 'auto',
                    width: '100%'
                  }}
                >
                  <Table hover className="mb-0" style={{ minWidth: '400px' }}>
                    <thead className="table-light">
                    <tr>
                        <th style={{ width: '5%' }}>CIF No</th>
                        <th style={{ width: '5%' }}>Customer Name</th>
                        <th style={{ width: '5%' }}>Segment Code</th>
                        <th style={{ width: '5%' }}>Segment Category</th>
                        <th style={{ width: '5%' }}>Updated At</th>
                    </tr>
                    </thead>
                    <tbody>
                    {segments.length > 0 ? (
                        segments.map((s) => (
                        <tr key={s.cifNo}>
                            <td style={{ width: '5%' }}>{s.cifNo}</td>
                            <td style={{ width: '5%' }}>{s.customerName}</td>
                            <td style={{ width: '5%' }}>{s.segmentCode}</td>
                            <td style={{ width: '5%' }}>{s.segmentCategory}</td>
                            <td style={{ width: '5%' }}>{new Date(s.updatedAt).toLocaleString()}</td>
                        </tr>
                        ))
                    ) : (
                        <tr>
                        <td colSpan="5" className="text-center py-4 text-muted fst-italic">
                            No data available.
                        </td>
                        </tr>
                    )}
                    </tbody>
                </Table>
                </div>
            </Card.Body>
            </Card>
        </Col>
        </Row>

    </Container>
  );
};

export default Segment;
