import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import Button from '../../src/core/components/Button';

describe('Button Component', () => {
  const defaultProps = {
    onPress: jest.fn(),
    children: 'Test Button',
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders correctly with default props', () => {
    const { getByText } = render(<Button {...defaultProps} />);
    expect(getByText('Test Button')).toBeTruthy();
  });

  it('calls onPress when pressed', () => {
    const { getByText } = render(<Button {...defaultProps} />);
    const button = getByText('Test Button');
    
    fireEvent.press(button);
    expect(defaultProps.onPress).toHaveBeenCalledTimes(1);
  });

  it('renders correctly with primary variant', () => {
    const { getByText } = render(
      <Button {...defaultProps} variant="primary" />
    );
    expect(getByText('Test Button')).toBeTruthy();
  });

  it('renders correctly with secondary variant', () => {
    const { getByText } = render(
      <Button {...defaultProps} variant="secondary" />
    );
    expect(getByText('Test Button')).toBeTruthy();
  });

  it('renders correctly with large size', () => {
    const { getByText } = render(
      <Button {...defaultProps} size="large" />
    );
    expect(getByText('Test Button')).toBeTruthy();
  });

  it('renders correctly with small size', () => {
    const { getByText } = render(
      <Button {...defaultProps} size="small" />
    );
    expect(getByText('Test Button')).toBeTruthy();
  });

  it('disables button when disabled prop is true', () => {
    const { getByText } = render(
      <Button {...defaultProps} disabled />
    );
    const button = getByText('Test Button');
    
    // Check if button is disabled by trying to press it
    fireEvent.press(button);
    expect(defaultProps.onPress).not.toHaveBeenCalled();
  });

  it('shows loading state when loading prop is true', () => {
    const { queryByText } = render(
      <Button {...defaultProps} loading />
    );
    // Loading state should hide button text
    expect(queryByText('Test Button')).toBeNull();
  });

  it('renders with start icon when provided', () => {
    const { getByTestId } = render(
      <Button {...defaultProps} startIcon="add" />
    );
    expect(getByTestId('button-start-icon')).toBeTruthy();
  });

  it('renders with end icon when provided', () => {
    const { getByTestId } = render(
      <Button {...defaultProps} endIcon="arrow-forward" />
    );
    expect(getByTestId('button-end-icon')).toBeTruthy();
  });

  it('applies custom styles when provided', () => {
    const customStyle = { backgroundColor: 'red' };
    const { getByText } = render(
      <Button {...defaultProps} style={customStyle} />
    );
    expect(getByText('Test Button')).toBeTruthy();
  });

  it('passes accessibility props correctly', () => {
    const accessibilityProps = {
      accessibilityLabel: 'Custom button',
      accessibilityHint: 'Press to submit',
    };
    const { getByText } = render(
      <Button {...defaultProps} {...accessibilityProps} />
    );
    expect(getByText('Test Button')).toBeTruthy();
  });
});