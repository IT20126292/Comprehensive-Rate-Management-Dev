import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Form, Button, Alert } from 'react-bootstrap';
import { saveDefineSegment, fetchSegmentByCode } from '../api/api';
import axios from 'axios';

const SegmentDefinitionForm = () => {
  const [status, setStatus] = useState('');
  const [dropdownData, setDropdownData] = useState({
    segmentBasis: [],
    mainSegmentCodes: [],
    upgradeCriteria: [],
    downgradeCriteria: []
  });

  const [segment, setSegment] = useState({
    code: '',
    mainSegment: '',
    name: '',
    onInvitation: false,
    depositMin: '',
    depositMax: '',
    salaryMin: '',
    salaryMax: '',
    segmentBasis: null,
    mainSegmentCode: null,
    subSegmentCode: '',
    subSegmentName: '',
    upgradeCriteria: null,
    downgradeCriteria: null
  });

  // Fetch dropdown values from API
  useEffect(() => {
    const fetchDropdowns = async () => {
      try {
        const [basisRes, mainSegRes, upgradeRes, downgradeRes] = await Promise.all([
          axios.get('http://localhost:8080/api/segments/data/segment-basis'),
          axios.get('http://localhost:8080/api/segments/data/main-segments'),
          axios.get('http://localhost:8080/api/segments/data/upgrade-criteria'),
          axios.get('http://localhost:8080/api/segments/data/downgrade-criteria')
        ]);

        setDropdownData({
          segmentBasis: basisRes.data || [],
          mainSegmentCodes: mainSegRes.data || [],
          upgradeCriteria: upgradeRes.data || [],
          downgradeCriteria: downgradeRes.data || []
        });
      } catch (err) {
        console.error('Failed to load dropdowns', err);
      }
    };
    fetchDropdowns();
  }, []);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;

    switch (name) {
      case 'segmentBasis':
        setSegment(prev => ({
          ...prev,
          segmentBasis: dropdownData.segmentBasis.find(item => item.shortCode === value) || null
        }));
        break;

      case 'mainSegmentCode':
        setSegment(prev => ({
          ...prev,
          mainSegmentCode: dropdownData.mainSegmentCodes.find(item => item.shortCode === value) || null
        }));
        break;

      case 'upgradeCriteria':
        setSegment(prev => ({
          ...prev,
          upgradeCriteria: dropdownData.upgradeCriteria.find(item => item.shortCode === value) || null
        }));
        break;

      case 'downgradeCriteria':
        setSegment(prev => ({
          ...prev,
          downgradeCriteria: dropdownData.downgradeCriteria.find(item => item.shortCode === value) || null
        }));
        break;

      default:
        setSegment(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
    }
  };

  const handleSaveDefineSegment = async () => {
    if (
      !segment.code.trim() ||
      !segment.mainSegment.trim() ||
      !segment.name.trim() ||
      !segment.segmentBasis ||
      !segment.mainSegmentCode
    ) {
      setStatus('❌ Please fill all mandatory fields.');
      return;
    }

    try {
      await saveDefineSegment(segment);
      setStatus('✅ Segment definition saved successfully.');
    } catch (err) {
      console.error(err);
      setStatus('❌ Failed to save segment definition.');
    }
  };

  const handleReset = () => {
    setSegment({
      code: '',
      mainSegment: '',
      name: '',
      onInvitation: false,
      depositMin: '',
      depositMax: '',
      salaryMin: '',
      salaryMax: '',
      segmentBasis: null,
      mainSegmentCode: null,
      subSegmentCode: '',
      subSegmentName: '',
      upgradeCriteria: null,
      downgradeCriteria: null
    });
    setStatus('✅ Data cleared successfully.');
  };

  return (
    <Container fluid className="pt-5" style={{ paddingLeft: '280px', backgroundColor: '#f4f6f9', minHeight: '100vh' }}>
      <Row className="mb-4">
        <Col>
          <h5 className="text-center fw-bold text-primary">Define Customer Segment</h5>
        </Col>
      </Row>

      <Row className="justify-content-center">
        <Col md={10}>
          <Card className="shadow-lg border-0 rounded-4">
            <Card.Header className="bg-primary text-white fw-semibold fs-10 rounded-top">
              Segment Definition Form
            </Card.Header>

            {status && (
              <Row className="mt-3 justify-content-center">
                <Col md={8}>
                  <Alert variant={status.includes('✅') ? 'success' : 'danger'} className="text-center shadow-sm" dismissible>
                    {status}
                  </Alert>
                </Col>
              </Row>
            )}

            <Card.Body className="p-4">
              <Form>
                <Row className="mb-3">
                  <Col md={6}>
                    <Form.Label className="fw-semibold">Segment Basis</Form.Label>
                    <Form.Select
                      name="segmentBasis"
                      value={segment.segmentBasis?.shortCode || ''}
                      onChange={handleChange}
                    >
                      <option value="">Select</option>
                      {dropdownData.segmentBasis.map(opt => (
                        <option key={opt.segBasisCode} value={opt.shortCode}>
                          {opt.codeDescription}
                        </option>
                      ))}
                    </Form.Select>
                  </Col>

                  <Col md={6}>
                    <Form.Label className="fw-semibold">Main Segment Code</Form.Label>
                    <Form.Select
                      name="mainSegmentCode"
                      value={segment.mainSegmentCode?.code || ''}
                      onChange={handleChange}
                    >
                      <option value="">Select</option>
                      {dropdownData.mainSegmentCodes.map(opt => (
                        <option key={opt.mainSegCode} value={opt.shortCode}>{opt.codeDescription}</option>
                      ))}
                    </Form.Select>
                  </Col>
                </Row>

                <Row className="mb-3">
                  <Col md={6}>
                    <Form.Label className="fw-semibold">Upgrade Criteria</Form.Label>
                    <Form.Select
                      name="upgradeCriteria"
                      value={segment.upgradeCriteria?.code || ''}
                      onChange={handleChange}
                    >
                      <option value="">Select</option>
                      {dropdownData.upgradeCriteria.map(opt => (
                        <option key={opt.logicCode} value={opt.shortCode}>{opt.codeDescription}</option>
                      ))}
                    </Form.Select>
                  </Col>

                  <Col md={6}>
                    <Form.Label className="fw-semibold">Downgrade Criteria</Form.Label>
                    <Form.Select
                      name="downgradeCriteria"
                      value={segment.downgradeCriteria?.code || ''}
                      onChange={handleChange}
                    >
                      <option value="">Select</option>
                      {dropdownData.downgradeCriteria.map(opt => (
                        <option key={opt.logicCode} value={opt.shortCode}>{opt.codeDescription}</option>
                      ))}
                    </Form.Select>
                  </Col>
                </Row>

                {/* Sub Segment Code / Name */}
                <Row className="mb-3">
                  <Col md={6}>
                    <Form.Label className="fw-semibold">Sub Segment Code</Form.Label>
                    <Form.Control
                      name="subSegmentCode"
                      value={segment.subSegmentCode}
                      onChange={handleChange}
                      placeholder="Enter sub segment code"
                    />
                  </Col>
                  <Col md={6}>
                    <Form.Label className="fw-semibold">Sub Segment Name</Form.Label>
                    <Form.Control
                      name="subSegmentName"
                      value={segment.subSegmentName}
                      onChange={handleChange}
                      placeholder="Enter sub segment name"
                    />
                  </Col>
                </Row>

                {/* On Invitation */}
                <Row className="mb-3">
                  <Col md={4}>
                    <Form.Check
                      type="checkbox"
                      label="On Invitation"
                      name="onInvitation"
                      checked={segment.onInvitation}
                      onChange={handleChange}
                      className="fw-semibold"
                    />
                  </Col>
                </Row>

                {/* Deposit / Salary */}
                <Row className="mb-3">
                  <Col md={3}>
                    <Form.Label className="fw-semibold">Total Deposit Min</Form.Label>
                    <Form.Control
                      type="number"
                      name="depositMin"
                      value={segment.depositMin}
                      onChange={handleChange}
                      disabled={segment.salaryMin || segment.salaryMax}
                      placeholder="Min"
                    />
                  </Col>
                  <Col md={3}>
                    <Form.Label className="fw-semibold">Total Deposit Max</Form.Label>
                    <Form.Control
                      type="number"
                      name="depositMax"
                      value={segment.depositMax}
                      onChange={handleChange}
                      disabled={segment.salaryMin || segment.salaryMax}
                      placeholder="Max"
                    />
                  </Col>
                  <Col md={3}>
                    <Form.Label className="fw-semibold">Salary Credit Min</Form.Label>
                    <Form.Control
                      type="number"
                      name="salaryMin"
                      value={segment.salaryMin}
                      onChange={handleChange}
                      disabled={segment.depositMin || segment.depositMax}
                      placeholder="Min"
                    />
                  </Col>
                  <Col md={3}>
                    <Form.Label className="fw-semibold">Salary Credit Max</Form.Label>
                    <Form.Control
                      type="number"
                      name="salaryMax"
                      value={segment.salaryMax}
                      onChange={handleChange}
                      disabled={segment.depositMin || segment.depositMax}
                      placeholder="Max"
                    />
                  </Col>
                </Row>

                {/* Action Buttons */}
                <div className="d-flex gap-3 mt-4 justify-content-end">
                  <Button variant="success" onClick={handleSaveDefineSegment}>Create Segment</Button>
                  <Button variant="outline-danger" onClick={handleReset}>Reset</Button>
                </div>
              </Form>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default SegmentDefinitionForm;
